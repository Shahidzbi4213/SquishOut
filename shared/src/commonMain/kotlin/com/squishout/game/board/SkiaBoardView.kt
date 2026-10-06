package com.squishout.game.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.squishout.engine.model.Board
import com.squishout.engine.model.Direction
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.Position
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun SkiaBoardView(
    board: Board,
    highlightedJellyId: String? = null,
    onTileTapped: (Position) -> Unit,
    modifier: Modifier = Modifier,
    stage: Int = 1,
    activeLaunchesFlow: StateFlow<List<LaunchAnimation>>? = null,
    wobbleOffsetsFlow: StateFlow<Map<String, Float>>? = null
) {
    val activeLaunches by (activeLaunchesFlow?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf(emptyList()) })
    val wobbleOffsets by (wobbleOffsetsFlow?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf(emptyMap()) })
    val biome = com.squishout.game.theme.BiomeTheme.forStage(stage)

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

        Canvas(
            modifier = Modifier
                .aspectRatio(1f)
                .pointerInput(board) {
                    detectTapGestures { tapOffset ->
                        val localX = tapOffset.x - trayPadding
                        val localY = tapOffset.y - trayPadding

                        // Guard against bezel taps outside grid area
                        if (localX < 0f || localY < 0f) return@detectTapGestures

                        val col = (localX / tileSize).toInt()
                        val row = (localY / tileSize).toInt()

                        if (col in 0 until board.width && row in 0 until board.height) {
                            onTileTapped(Position(col, row))
                        }
                    }
                }
        ) {
            val currentLaunches = activeLaunches
            val currentWobbles = wobbleOffsets

            // 1. Draw Glazed Porcelain Tray & Recessed Inset Wells with Biome Accent
            TrayRenderer.drawTray(
                drawScope = this,
                boardWidth = board.width,
                boardHeight = board.height,
                tileSize = tileSize,
                trayPadding = trayPadding,
                biome = biome
            )

            // 2. Draw Obstacles
            for (obs in board.obstacles) {
                val topLeft = Offset(
                    trayPadding + obs.position.x * tileSize + tileSize * 0.08f,
                    trayPadding + obs.position.y * tileSize + tileSize * 0.08f
                )
                TrayRenderer.drawObstacle(this, obs, topLeft, tileSize)
            }

            // 3. Draw Stationary Jellies on the Board
            for (jelly in board.jellies) {
                val minX = jelly.tiles.minOf { it.x }
                val minY = jelly.tiles.minOf { it.y }
                val topLeft = Offset(
                    trayPadding + minX * tileSize + tileSize * 0.08f,
                    trayPadding + minY * tileSize + tileSize * 0.08f
                )

                val wobble = currentWobbles[jelly.id] ?: 0f
                val wobbleX = if (jelly.direction == Direction.EAST || jelly.direction == Direction.WEST) wobble * tileSize * 0.12f else 0f
                val wobbleY = if (jelly.direction == Direction.NORTH || jelly.direction == Direction.SOUTH) wobble * tileSize * 0.12f else 0f

                JellyRenderer.drawJelly(
                    drawScope = this,
                    jelly = jelly,
                    topLeft = topLeft,
                    tileSize = tileSize,
                    offsetX = wobbleX,
                    offsetY = wobbleY,
                    isHighlighted = jelly.id == highlightedJellyId
                )
            }

            // 4. Draw Active Launching Jellies (Smooth Physics & Squish-and-Stretch)
            for (launch in currentLaunches) {
                val minX = launch.jelly.tiles.minOf { it.x }
                val minY = launch.jelly.tiles.minOf { it.y }
                val startX = trayPadding + minX * tileSize + tileSize * 0.08f
                val startY = trayPadding + minY * tileSize + tileSize * 0.08f

                val maxDimension = maxOf(board.width, board.height)
                val exitDistance = tileSize * (maxDimension + 4f)
                val traveled = exitDistance * launch.easedProgress

                val currentOffset = Offset(
                    startX + launch.jelly.direction.dx * traveled,
                    startY + launch.jelly.direction.dy * traveled
                )

                val isHorizontal = launch.jelly.direction == Direction.EAST || launch.jelly.direction == Direction.WEST
                val scaleX = if (isHorizontal) launch.scaleAlongDir else launch.scalePerpendicular
                val scaleY = if (isHorizontal) launch.scalePerpendicular else launch.scaleAlongDir

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
        }
    }
}
