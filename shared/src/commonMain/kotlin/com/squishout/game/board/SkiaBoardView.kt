package com.squishout.game.board

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.squishout.engine.model.Board
import com.squishout.engine.model.Direction
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.Obstacle
import com.squishout.engine.model.Position
import com.squishout.game.theme.SquishColors
import com.squishout.game.util.currentTimeMillis
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun SkiaBoardView(
    board: Board,
    highlightedJellyId: String? = null,
    onTileTapped: (Position) -> Unit,
    modifier: Modifier = Modifier,
    stage: Int = 1,
    activeLaunchesFlow: StateFlow<List<LaunchAnimation>>? = null,
    wobbleOffsetsFlow: StateFlow<Map<String, Float>>? = null,
    blockerRecoilsFlow: StateFlow<Map<Position, Float>>? = null,
    flyingRewardsFlow: StateFlow<List<FlyingRewardToken>>? = null,
    shatteredObstaclesFlow: StateFlow<List<Obstacle>>? = null
) {
    val activeLaunches by (activeLaunchesFlow?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val wobbleOffsets by (wobbleOffsetsFlow?.collectAsState() ?: remember { mutableStateOf(emptyMap()) })
    val blockerRecoils by (blockerRecoilsFlow?.collectAsState() ?: remember { mutableStateOf(emptyMap()) })
    val flyingRewards by (flyingRewardsFlow?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val shatteredObstacles by (shatteredObstaclesFlow?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val biome = com.squishout.game.theme.BiomeTheme.forStage(stage)

    // 1. Continuous DrawPhase Animation Clock (Skipping UI recomposition)
    val infiniteTransition = rememberInfiniteTransition(label = "BoardAnimationClock")
    val animClockSeconds by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ClockSeconds"
    )

    // 2. High-throughput Allocation-Free Particle Pool
    val particleSystem = remember { ParticleSystem(240) }

    // 3. Touch Anticipation & Look-Around State
    var pressedJellyId by remember { mutableStateOf<String?>(null) }
    var activeTouchOffset by remember { mutableStateOf<Offset?>(null) }

    // 4. Cascading Board Entry Drop-In Timer (Triggered only when entering or resetting a stage)
    val stageStartTime = remember(stage) { currentTimeMillis() }

    // 5. Track rim splash and takeoff bursts to fire once per launching jelly
    val firedRimSplashes = remember { mutableSetOf<String>() }
    val firedTakeoffRings = remember { mutableSetOf<String>() }

    // Clear particle caches on stage change
    LaunchedEffect(stage) {
        particleSystem.clear()
        firedRimSplashes.clear()
        firedTakeoffRings.clear()
    }

    LaunchedEffect(activeLaunches.isEmpty()) {
        if (activeLaunches.isEmpty()) {
            firedTakeoffRings.clear()
        }
    }

    // React to shattered obstacles
    LaunchedEffect(shatteredObstacles) {
        if (shatteredObstacles.isNotEmpty()) {
            // Handled inside draw phase when tile size is known
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .padding(16.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val totalBoardSlots = board.width.toFloat()
        // Tray padding around board: ~0.15 of a tile
        val trayPaddingRatio = 0.15f
        val virtualSlots = totalBoardSlots + trayPaddingRatio * 2f

        val availableSize = minOf(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val tileSize = availableSize / virtualSlots
        val trayPadding = tileSize * trayPaddingRatio

        // Trigger shattered obstacle bursts if any arrived
        val currentShattered = shatteredObstacles
        if (currentShattered.isNotEmpty()) {
            for (obs in currentShattered) {
                val cx = trayPadding + obs.position.x * tileSize + tileSize / 2f
                val cy = trayPadding + obs.position.y * tileSize + tileSize / 2f
                particleSystem.spawnObstacleShatter(cx, cy, SquishColors.SlateObstacle, 16)
            }
        }

        Canvas(
            modifier = Modifier
                .aspectRatio(1f)
                .pointerInput(board) {
                    detectTapGestures(
                        onPress = { pressOffset ->
                            activeTouchOffset = pressOffset
                            val localX = pressOffset.x - trayPadding
                            val localY = pressOffset.y - trayPadding
                            if (localX >= 0f && localY >= 0f) {
                                val col = (localX / tileSize).toInt()
                                val row = (localY / tileSize).toInt()
                                if (col in 0 until board.width && row in 0 until board.height) {
                                    val pressedJelly = board.getJellyAt(Position(col, row))
                                    if (pressedJelly != null) {
                                        pressedJellyId = pressedJelly.id
                                    }
                                }
                            }
                            tryAwaitRelease()
                            pressedJellyId = null
                            activeTouchOffset = null
                        },
                        onTap = { tapOffset ->
                            val localX = tapOffset.x - trayPadding
                            val localY = tapOffset.y - trayPadding
                            if (localX < 0f || localY < 0f) return@detectTapGestures

                            val col = (localX / tileSize).toInt()
                            val row = (localY / tileSize).toInt()
                            if (col in 0 until board.width && row in 0 until board.height) {
                                onTileTapped(Position(col, row))
                            }
                        }
                    )
                }
        ) {
            val now = currentTimeMillis()
            val clockSeconds = animClockSeconds
            val currentLaunches = activeLaunches
            val currentWobbles = wobbleOffsets
            val currentRecoils = blockerRecoils
            val currentRewards = flyingRewards

            // Calculate cascading drop-in entrance progress
            val dropElapsed = now - stageStartTime
            val rowDelayMs = 38L
            val dropDurationMs = 280L

            fun getCascadeOffsetY(row: Int): Float {
                if (dropElapsed > 1200L) return 0f
                val rowDelay = row * rowDelayMs
                return if (dropElapsed < rowDelay) {
                    -availableSize * 0.85f // Suspended above board
                } else {
                    val p = ((dropElapsed - rowDelay).toFloat() / dropDurationMs).coerceIn(0f, 1f)
                    if (p < 1f) {
                        // Juicy bounce overshoot
                        val bounce = sin(p * PI.toFloat()) * tileSize * 0.16f
                        -(1f - p) * (availableSize * 0.85f) + bounce
                    } else 0f
                }
            }

            // Check if fever climax launch is active
            val climaxLaunch = currentLaunches.firstOrNull { it.isFeverClimax }
            val shockwaveCenter = if (climaxLaunch != null) {
                val minX = climaxLaunch.jelly.tiles.minOf { it.x }
                val minY = climaxLaunch.jelly.tiles.minOf { it.y }
                Offset(trayPadding + minX * tileSize + tileSize / 2f, trayPadding + minY * tileSize + tileSize / 2f)
            } else null
            val shockwaveRadius = if (climaxLaunch != null) climaxLaunch.easedProgress * availableSize * 0.85f else 0f
            val shockwaveAlpha = if (climaxLaunch != null) (1f - climaxLaunch.progress).coerceIn(0f, 1f) else 0f

            // Micro-Impulse Tray Screen Shake during collisions and fever climax
            val maxWobble = currentWobbles.values.maxOrNull() ?: 0f
            val maxRecoil = currentRecoils.values.maxOrNull() ?: 0f
            val impactImpulse = maxOf(maxWobble, maxRecoil)
            val shakeMagnitude = if (impactImpulse > 0.04f) {
                impactImpulse * tileSize * 0.055f // 2-4px crunchy shudder
            } else if (climaxLaunch != null && climaxLaunch.progress < 0.35f) {
                (1f - climaxLaunch.progress / 0.35f) * tileSize * 0.075f
            } else 0f

            val shakeX = if (shakeMagnitude > 0f) sin(clockSeconds * 80f) * shakeMagnitude else 0f
            val shakeY = if (shakeMagnitude > 0f) kotlin.math.cos(clockSeconds * 95f) * shakeMagnitude * 0.85f else 0f

            withTransform({
                translate(shakeX, shakeY)
            }) {
                // 1. Draw Glazed Porcelain Tray & Recessed Inset Wells with Biome Accent & Climax Shockwave
                TrayRenderer.drawTray(
                    drawScope = this,
                    boardWidth = board.width,
                    boardHeight = board.height,
                    tileSize = tileSize,
                    trayPadding = trayPadding,
                    biome = biome,
                    shockwaveCenter = shockwaveCenter,
                    shockwaveRadius = shockwaveRadius,
                    shockwaveAlpha = shockwaveAlpha
                )

                // 1b. Touch Anticipation Trajectory (Glowing exit raycast or amber tension blocker pip)
                val currentPressedId = pressedJellyId
                if (currentPressedId != null) {
                    val pressedJelly = board.getJellyById(currentPressedId)
                    if (pressedJelly != null) {
                        drawAnticipationTrajectory(
                            drawScope = this,
                            board = board,
                            jelly = pressedJelly,
                            tileSize = tileSize,
                            trayPadding = trayPadding,
                            animClockSeconds = clockSeconds
                        )
                    }
                }

                // 2. Draw Water Jets (90° Trajectory Deflector Conveyors)
                for (jet in board.waterJets) {
                    val dropOffset = getCascadeOffsetY(jet.position.y)
                    val topLeft = Offset(
                        trayPadding + jet.position.x * tileSize + tileSize * 0.08f,
                        trayPadding + jet.position.y * tileSize + tileSize * 0.08f + dropOffset
                    )
                    TrayRenderer.drawWaterJet(this, jet, topLeft, tileSize)
                }

                // 3. Draw Obstacles (with Blocker Impact Recoil Shudder and Drop-in Entrance)
                for (obs in board.obstacles) {
                    val dropOffset = getCascadeOffsetY(obs.position.y)
                    val recoil = currentRecoils[obs.position] ?: 0f
                    val recoilOffset = recoil * tileSize * 0.14f

                    val topLeft = Offset(
                        trayPadding + obs.position.x * tileSize + tileSize * 0.08f,
                        trayPadding + obs.position.y * tileSize + tileSize * 0.08f + dropOffset
                    )
                    TrayRenderer.drawObstacle(
                        drawScope = this,
                        obstacle = obs,
                        topLeft = topLeft,
                        tileSize = tileSize,
                        offsetX = recoilOffset,
                        offsetY = recoilOffset
                    )
                }

                // 4. Draw Symbiotic Schooling Pair Tethers
                val drawnTethers = mutableSetOf<String>()
                for (jelly in board.jellies) {
                    val partnerId = jelly.linkedJellyId ?: continue
                    val pairKey = if (jelly.id < partnerId) "${jelly.id}_$partnerId" else "${partnerId}_${jelly.id}"
                    if (pairKey in drawnTethers) continue
                    drawnTethers.add(pairKey)

                    val partner = board.getJellyById(partnerId) ?: continue
                    val drop1 = getCascadeOffsetY(jelly.tiles.first().y)
                    val drop2 = getCascadeOffsetY(partner.tiles.first().y)
                    val c1 = Offset(
                        trayPadding + jelly.tiles.first().x * tileSize + tileSize / 2f,
                        trayPadding + jelly.tiles.first().y * tileSize + tileSize / 2f + drop1
                    )
                    val c2 = Offset(
                        trayPadding + partner.tiles.first().x * tileSize + tileSize / 2f,
                        trayPadding + partner.tiles.first().y * tileSize + tileSize / 2f + drop2
                    )
                    JellyRenderer.drawCandyTether(
                        drawScope = this,
                        p1 = c1,
                        p2 = c2,
                        tileSize = tileSize,
                        isAwake = jelly.eyeState == com.squishout.engine.model.EyeState.AWAKE
                    )
                }

                // 5. Draw Stationary Jellies (Idle Breathing, Blinking, Look-Around, Press Anticipation & Defender Recoil)
                for (jelly in board.jellies) {
                    val minX = jelly.tiles.minOf { it.x }
                    val minY = jelly.tiles.minOf { it.y }
                    val dropOffset = getCascadeOffsetY(minY)

                    val topLeft = Offset(
                        trayPadding + minX * tileSize + tileSize * 0.08f,
                        trayPadding + minY * tileSize + tileSize * 0.08f + dropOffset
                    )

                    val wobble = currentWobbles[jelly.id] ?: 0f
                    // Defender jelly recoil: if another jelly hit this jelly's tiles
                    val struckRecoil = jelly.tiles.firstNotNullOfOrNull { currentRecoils[it] } ?: 0f
                    val effectiveWobble = if (kotlin.math.abs(wobble) > 0.01f) wobble else struckRecoil
                    val wobbleX = if (jelly.direction == Direction.EAST || jelly.direction == Direction.WEST) effectiveWobble * tileSize * 0.12f else 0f
                    val wobbleY = if (jelly.direction == Direction.NORTH || jelly.direction == Direction.SOUTH) effectiveWobble * tileSize * 0.12f else 0f

                    val isPressed = jelly.id == pressedJellyId

                    JellyRenderer.drawJelly(
                        drawScope = this,
                        jelly = jelly,
                        topLeft = topLeft,
                        tileSize = tileSize,
                        offsetX = wobbleX,
                        offsetY = wobbleY,
                        isHighlighted = jelly.id == highlightedJellyId,
                        animTimeSeconds = clockSeconds,
                        isPressed = isPressed,
                        wobbleOffset = wobble,
                        recoilOffset = struckRecoil,
                        touchPoint = activeTouchOffset
                    )

                    // Ambient fizzy carbonation bubbles for unblocked awake jellies
                    if (jelly.eyeState == com.squishout.engine.model.EyeState.AWAKE) {
                        val phase = (jelly.id.hashCode() and 0xFFFF) * 0.001f
                        val fizzCycle = (clockSeconds * 1.8f + phase * 4f) % 2.5f
                        if (fizzCycle < 0.018f) {
                            val jcx = trayPadding + minX * tileSize + tileSize / 2f
                            val jcy = trayPadding + minY * tileSize + tileSize / 2f
                            val palette = JellyRenderer.getPalette(jelly.type)
                            particleSystem.spawnCarbonationBubbles(jcx, jcy, palette.second, count = 2)
                        }
                    }
                }

                // 5b. Blocker Target Reticle (drawn over blocking entities)
                if (currentPressedId != null) {
                    val pressedJelly = board.getJellyById(currentPressedId)
                    if (pressedJelly != null) {
                        drawBlockerTargetPip(
                            drawScope = this,
                            board = board,
                            jelly = pressedJelly,
                            tileSize = tileSize,
                            trayPadding = trayPadding,
                            animClockSeconds = clockSeconds
                        )
                    }
                }

            // 6. Draw Active Launching Jellies (Droplets, Gummy Wake Trail, Rim Breach Splash, Fever Climax)
            for (launch in currentLaunches) {
                val isDeflected = launch.exitPath.size >= 2
                val currentOffset: Offset
                val currentDir: Direction

                if (isDeflected) {
                    val totalSegments = (launch.exitPath.size - 1).coerceAtLeast(1)
                    val rawIndex = launch.easedProgress * totalSegments
                    val segIndex = rawIndex.toInt().coerceIn(0, totalSegments - 1)
                    val subProgress = (rawIndex - segIndex).coerceIn(0f, 1f)

                    val p1 = launch.exitPath[segIndex]
                    val p2 = launch.exitPath[minOf(segIndex + 1, launch.exitPath.size - 1)]

                    val interpX = p1.x + (p2.x - p1.x) * subProgress
                    val interpY = p1.y + (p2.y - p1.y) * subProgress

                    currentOffset = Offset(
                        trayPadding + interpX * tileSize + tileSize * 0.08f,
                        trayPadding + interpY * tileSize + tileSize * 0.08f
                    )

                    val dx = p2.x - p1.x
                    val dy = p2.y - p1.y
                    currentDir = when {
                        dx > 0 -> Direction.EAST
                        dx < 0 -> Direction.WEST
                        dy > 0 -> Direction.SOUTH
                        else -> Direction.NORTH
                    }
                } else {
                    val minX = launch.jelly.tiles.minOf { it.x }
                    val minY = launch.jelly.tiles.minOf { it.y }
                    val startX = trayPadding + minX * tileSize + tileSize * 0.08f
                    val startY = trayPadding + minY * tileSize + tileSize * 0.08f

                    val maxDimension = maxOf(board.width, board.height)
                    val exitDistance = tileSize * (maxDimension + 4f)
                    val traveled = exitDistance * launch.easedProgress

                    currentOffset = Offset(
                        startX + launch.jelly.direction.dx * traveled,
                        startY + launch.jelly.direction.dy * traveled
                    )
                    currentDir = launch.jelly.direction
                }

                val isHorizontal = currentDir == Direction.EAST || currentDir == Direction.WEST
                val scaleX = if (isHorizontal) launch.scaleAlongDir else launch.scalePerpendicular
                val scaleY = if (isHorizontal) launch.scalePerpendicular else launch.scaleAlongDir
                val jellyColor = JellyRenderer.getPalette(launch.jelly.type).first

                // A0. Takeoff Gelatin Splatter Wave Ring
                if (launch.progress in 0.01f..0.15f && firedTakeoffRings.add(launch.jelly.id)) {
                    val minX = launch.jelly.tiles.minOf { it.x }
                    val minY = launch.jelly.tiles.minOf { it.y }
                    val takeoffX = trayPadding + minX * tileSize + tileSize / 2f
                    val takeoffY = trayPadding + minY * tileSize + tileSize / 2f
                    particleSystem.spawnSplatterRing(
                        originX = takeoffX,
                        originY = takeoffY,
                        color = jellyColor,
                        baseRadius = tileSize * 0.45f
                    )
                }

                // A. Backward Rocket Juice Droplets (exhaust stream)
                if (launch.progress in 0.08f..0.7f) {
                    particleSystem.spawnJuiceDroplets(
                        originX = currentOffset.x + tileSize / 2f,
                        originY = currentOffset.y + tileSize / 2f,
                        color = jellyColor,
                        direction = currentDir,
                        count = 2
                    )
                }

                // B. Tray Rim Breach Splash
                if (launch.progress >= 0.72f && firedRimSplashes.add(launch.jelly.id)) {
                    particleSystem.spawnRimSplash(
                        originX = currentOffset.x + tileSize / 2f,
                        originY = currentOffset.y + tileSize / 2f,
                        color = jellyColor,
                        exitDir = currentDir,
                        count = 10
                    )
                    if (launch.isFeverClimax) {
                        particleSystem.spawnStarBurst(
                            originX = currentOffset.x + tileSize / 2f,
                            originY = currentOffset.y + tileSize / 2f,
                            count = 24
                        )
                    }
                }

                // C. Gummy Motion Blur Wake (trailing ghost silhouettes)
                if (launch.progress in 0.08f..0.85f) {
                    val trail1Dist = tileSize * 0.32f
                    val trail1 = Offset(currentOffset.x - currentDir.dx * trail1Dist, currentOffset.y - currentDir.dy * trail1Dist)
                    JellyRenderer.drawJelly(
                        drawScope = this,
                        jelly = launch.jelly,
                        topLeft = trail1,
                        tileSize = tileSize,
                        scaleX = scaleX * 0.94f,
                        scaleY = scaleY * 0.94f,
                        alpha = launch.alpha * 0.32f
                    )

                    val trail2Dist = tileSize * 0.65f
                    val trail2 = Offset(currentOffset.x - currentDir.dx * trail2Dist, currentOffset.y - currentDir.dy * trail2Dist)
                    JellyRenderer.drawJelly(
                        drawScope = this,
                        jelly = launch.jelly,
                        topLeft = trail2,
                        tileSize = tileSize,
                        scaleX = scaleX * 0.86f,
                        scaleY = scaleY * 0.86f,
                        alpha = launch.alpha * 0.16f
                    )
                }

                // D. Primary High-Speed Jelly Body
                JellyRenderer.drawJelly(
                    drawScope = this,
                    jelly = launch.jelly,
                    topLeft = currentOffset,
                    tileSize = tileSize,
                    scaleX = scaleX,
                    scaleY = scaleY,
                    alpha = launch.alpha
                )
            }

            // 7. Draw Bubble Fog / Ink Clouds over obscured cells
            for (fogPos in board.fogTiles) {
                val dropOffset = getCascadeOffsetY(fogPos.y)
                val topLeft = Offset(
                    trayPadding + fogPos.x * tileSize + tileSize * 0.04f,
                    trayPadding + fogPos.y * tileSize + tileSize * 0.04f + dropOffset
                )
                TrayRenderer.drawFogTile(this, topLeft, tileSize)
            }

            // 8. Draw Flying Reward Tokens (Stars & Candies Arcing via Quadratic Bezier)
            for (token in currentRewards) {
                val pos = token.currentPosition()
                val tokenScale = token.scale
                val starRadius = tileSize * 0.22f * tokenScale

                // Golden Glow Halo
                drawCircle(
                    color = SquishColors.StarGold.copy(alpha = 0.4f),
                    radius = starRadius * 1.5f,
                    center = pos
                )

                // 4-Point Golden Star Path
                val sPath = Path().apply {
                    moveTo(pos.x, pos.y - starRadius)
                    quadraticTo(pos.x, pos.y, pos.x + starRadius, pos.y)
                    quadraticTo(pos.x, pos.y, pos.x, pos.y + starRadius)
                    quadraticTo(pos.x, pos.y, pos.x - starRadius, pos.y)
                    quadraticTo(pos.x, pos.y, pos.x, pos.y - starRadius)
                    close()
                }
                drawPath(sPath, color = SquishColors.StarGold, style = Fill)
                drawPath(sPath, color = Color.White, style = Stroke(width = 2.5f))

                // Sparkle Core
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = starRadius * 0.35f,
                    center = pos
                )
            }

            // 9. Update & Draw Dynamic Particle System (Juice Droplets, Shards, Sparkles, Splatter Rings)
            particleSystem.updateAndDraw(this, 0.016f)
        }
    }
}
}

/**
 * Draws an interactive anticipation trajectory raycast when the player presses down on a jelly.
 * If unblocked, renders glowing chevrons flowing toward the tray rim with an exit beacon.
 * If blocked, renders a warning dashed tension line terminating at an amber/crimson blocker target pip.
 */
private fun drawAnticipationTrajectory(
    drawScope: DrawScope,
    board: Board,
    jelly: Jelly,
    tileSize: Float,
    trayPadding: Float,
    animClockSeconds: Float
) {
    val headPos = jelly.headPosition
    val headCenter = Offset(
        trayPadding + headPos.x * tileSize + tileSize / 2f,
        trayPadding + headPos.y * tileSize + tileSize / 2f
    )
    val escapePath = board.getEscapePath(jelly)
    val canEscape = board.canJellyEscape(jelly)

    var blockerPos: Position? = null
    val waypoints = mutableListOf<Offset>()
    waypoints.add(headCenter)

    for (pos in escapePath) {
        val waypointCenter = Offset(
            trayPadding + pos.x * tileSize + tileSize / 2f,
            trayPadding + pos.y * tileSize + tileSize / 2f
        )
        val otherJelly = board.getJellyAt(pos)
        val obstacle = board.getObstacleAt(pos)
        if ((otherJelly != null && otherJelly.id != jelly.id) || obstacle != null) {
            blockerPos = pos
            waypoints.add(waypointCenter)
            break
        }
        waypoints.add(waypointCenter)
    }

    drawScope.apply {
        if (canEscape) {
            // Clear exit trajectory: bright energetic luminous beam & glowing chevrons
            val (_, lightColor, _) = JellyRenderer.getPalette(jelly.type)

            for (i in 0 until waypoints.size - 1) {
                val p1 = waypoints[i]
                val p2 = waypoints[i + 1]

                // Soft wide outer glow
                drawLine(
                    color = lightColor.copy(alpha = 0.35f),
                    start = p1,
                    end = p2,
                    strokeWidth = tileSize * 0.30f,
                    cap = StrokeCap.Round
                )
                // Core bright beam
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = p1,
                    end = p2,
                    strokeWidth = tileSize * 0.10f,
                    cap = StrokeCap.Round
                )

                // Animated directional chevrons flowing along the beam
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                val segLen = hypot(dx, dy)
                if (segLen > 1f) {
                    val ndx = dx / segLen
                    val ndy = dy / segLen
                    val perpX = -ndy
                    val perpY = ndx
                    val step = tileSize * 0.44f
                    val flowOffset = (animClockSeconds * tileSize * 2.4f) % step
                    var d = flowOffset
                    val arm = tileSize * 0.11f

                    while (d < segLen) {
                        val cx = p1.x + ndx * d
                        val cy = p1.y + ndy * d
                        val chevronPath = Path().apply {
                            moveTo(cx - ndx * arm + perpX * arm, cy - ndy * arm + perpY * arm)
                            lineTo(cx, cy)
                            lineTo(cx - ndx * arm - perpX * arm, cy - ndy * arm - perpY * arm)
                        }
                        drawPath(
                            path = chevronPath,
                            color = Color.White.copy(alpha = 0.95f),
                            style = Stroke(width = tileSize * 0.045f, cap = StrokeCap.Round)
                        )
                        d += step
                    }
                }
            }

            // Pulsing exit beacon at board rim
            val exitPos = waypoints.last()
            val pulseRadius = tileSize * 0.35f + sin(animClockSeconds * 8f) * tileSize * 0.07f
            drawCircle(
                color = lightColor.copy(alpha = 0.45f),
                radius = pulseRadius,
                center = exitPos
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = tileSize * 0.15f,
                center = exitPos
            )
        } else {
            // Blocked trajectory: amber/crimson tension dashed line ending at blocker pip
            val warningColor = Color(0xFFFF5252)

            for (i in 0 until waypoints.size - 1) {
                val p1 = waypoints[i]
                val p2 = waypoints[i + 1]

                drawLine(
                    color = warningColor.copy(alpha = 0.65f),
                    start = p1,
                    end = p2,
                    strokeWidth = tileSize * 0.075f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(tileSize * 0.16f, tileSize * 0.12f),
                        phase = animClockSeconds * 25f
                    )
                )
            }
        }
    }
}

/**
 * Draws the high-visibility glowing blocker target reticle directly on top of the blocking entity.
 */
private fun drawBlockerTargetPip(
    drawScope: DrawScope,
    board: Board,
    jelly: Jelly,
    tileSize: Float,
    trayPadding: Float,
    animClockSeconds: Float
) {
    if (board.canJellyEscape(jelly)) return

    val escapePath = board.getEscapePath(jelly)
    var blockerPos: Position? = null
    for (pos in escapePath) {
        val otherJelly = board.getJellyAt(pos)
        val obstacle = board.getObstacleAt(pos)
        if ((otherJelly != null && otherJelly.id != jelly.id) || obstacle != null) {
            blockerPos = pos
            break
        }
    }

    if (blockerPos == null) return

    val warningColor = Color(0xFFFF4D4D)
    val bx = trayPadding + blockerPos.x * tileSize + tileSize / 2f
    val by = trayPadding + blockerPos.y * tileSize + tileSize / 2f
    val pulse = (sin(animClockSeconds * 10f) * 0.5f + 0.5f)

    drawScope.apply {
        // Outer warning glow halo
        drawCircle(
            color = warningColor.copy(alpha = 0.25f + 0.20f * pulse),
            radius = tileSize * 0.44f + pulse * tileSize * 0.08f,
            center = Offset(bx, by)
        )
        // Pulsing warning ring
        drawCircle(
            color = warningColor.copy(alpha = 0.65f + 0.35f * pulse),
            radius = tileSize * 0.38f + pulse * tileSize * 0.05f,
            center = Offset(bx, by),
            style = Stroke(width = tileSize * 0.055f)
        )
        // Center barrier cross / reticle
        val arm = tileSize * 0.14f
        drawLine(
            color = Color.White.copy(alpha = 0.95f),
            start = Offset(bx - arm, by - arm),
            end = Offset(bx + arm, by + arm),
            strokeWidth = tileSize * 0.055f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.95f),
            start = Offset(bx - arm, by + arm),
            end = Offset(bx + arm, by - arm),
            strokeWidth = tileSize * 0.055f,
            cap = StrokeCap.Round
        )
    }
}
