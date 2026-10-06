package com.squishout.game.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.squishout.game.data.entity.LevelRecordEntity
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.ui.components.SagaBottomNav
import kotlin.math.sin

data class SagaStage(
    val stageNumber: Int,
    val isUnlocked: Boolean,
    val isCurrent: Boolean,
    val stars: Int,
    val biome: String? = null
)

/**
 * 3D Candy-Arcade Saga Map Screen matching the Stitch visual overhaul.
 * Features a winding cookie-wafer path with stepping stones, 3D candy bevel level nodes,
 * an active level sunburst dome with crowned mascot, a carved wooden biome signpost,
 * a juicy arcade HUD header, and a floating toy-like navigation dock.
 */
@Composable
fun SagaMapScreen(
    session: UserSessionEntity,
    levelRecords: List<LevelRecordEntity>,
    onSelectStage: (Int) -> Unit,
    onNavigateToDex: () -> Unit,
    onNavigateToShop: () -> Unit = {},
    onOpenDailyReward: () -> Unit = {},
    canClaimDaily: Boolean = false,
    modifier: Modifier = Modifier
) {
    val totalStages = 50
    val recordMap = levelRecords.associateBy { it.levelNumber }
    val currentUnlocked = session.currentStage.coerceAtMost(totalStages)

    val stages = (1..totalStages).map { stageNum ->
        val record = recordMap[stageNum]
        val isUnlocked = stageNum <= currentUnlocked
        val isCurrent = stageNum == currentUnlocked
        val stars = record?.stars ?: 0
        val biome = when (stageNum) {
            1 -> "🌸 Sweet Meadow • Lvl 1–20"
            21 -> "🌊 Soda Lagoon • Lvl 21–40"
            41 -> "🍯 Honeycomb Valley • Lvl 41+"
            else -> null
        }
        SagaStage(stageNum, isUnlocked, isCurrent, stars, biome)
    }.reversed() // Reverse so level 1 starts at bottom and journey scrolls upwards

    val listState = rememberLazyListState()

    // Auto-scroll to current stage on load
    LaunchedEffect(currentUnlocked) {
        val index = (totalStages - currentUnlocked).coerceIn(0, totalStages - 1)
        listState.scrollToItem(index)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Meadow landscape gradient background
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB8F3FF), // Sky blue
                        Color(0xFFE2F9FE), // Soft horizon
                        Color(0xFFD8EEDC), // Distant pale mint hill
                        Color(0xFFC7E9C0), // Mid mint
                        Color(0xFFA7E8BD), // Foreground meadow
                        Color(0xFF8CE1A7)  // Deep rich grass
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background scenery elements (Clouds & Candies)
        MapBackgroundScenery()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Pinned Top Arcade HUD
            SagaHeader(
                session = session,
                onOpenDailyReward = onOpenDailyReward,
                canClaimDaily = canClaimDaily,
                onOpenShop = onNavigateToShop
            )

            // 2. Scrollable Winding Saga Journey
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                        CandyPeakSummit()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(
                        count = stages.size,
                        key = { index -> stages[index].stageNumber }
                    ) { index ->
                        val stage = stages[index]
                        val prevStage = stages.getOrNull(index - 1)
                        val nextStage = stages.getOrNull(index + 1)

                        val currentX = calculateStageXOffset(stage.stageNumber)
                        val prevX = prevStage?.let { calculateStageXOffset(it.stageNumber) }
                        val nextX = nextStage?.let { calculateStageXOffset(it.stageNumber) }

                        val itemHeight = when {
                            stage.biome != null -> 156.dp
                            stage.isCurrent -> 148.dp
                            else -> 100.dp
                        }
                        val nodeCenterY = if (stage.biome != null) 106.dp else (itemHeight / 2)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            // 3D Winding Cookie-Wafer Path chunk connecting continuously
                            ConnectingWaferPath(
                                prevX = prevX,
                                currentX = currentX,
                                nextX = nextX,
                                nodeCenterYDp = nodeCenterY
                            )

                            // Biome Milestone Banner placed over the continuous road
                            if (stage.biome != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 8.dp)
                                        .zIndex(2f)
                                ) {
                                    RusticWoodenSignpost(title = stage.biome)
                                }
                            }

                            // Interactive 3D Stage Node
                            StageNode(
                                stage = stage,
                                onClick = {
                                    if (stage.isUnlocked) {
                                        onSelectStage(stage.stageNumber)
                                    }
                                },
                                modifier = Modifier
                                    .align(if (stage.biome != null) Alignment.BottomCenter else Alignment.Center)
                                    .padding(bottom = if (stage.biome != null) 14.dp else 0.dp)
                                    .offset(x = currentX)
                                    .then(if (stage.isCurrent) Modifier.zIndex(3f) else Modifier.zIndex(1f))
                            )

                            // Decorative roadside candy props along the journey
                            RoadsideCandyDecorations(stageNumber = stage.stageNumber)
                        }
                    }

                    item {
                        // Bottom spacer for floating navigation dock
                        Spacer(modifier = Modifier.height(110.dp))
                    }
                }
            }
        }

        // 3. Floating Toy-Like Arcade Navigation Dock
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            SagaBottomNav(
                selectedTab = "map",
                onMapClick = {},
                onDexClick = onNavigateToDex,
                onShopClick = onNavigateToShop
            )
        }
    }
}

/**
 * Calculates sinusoidal X horizontal offset for a stage to create the classic winding S-curve.
 */
private fun calculateStageXOffset(stageNum: Int): Dp {
    return (sin(stageNum * 0.85f) * 80f).dp
}

/* =========================================================================
   TOP ARCADE JUICY HUD BAR
   ========================================================================= */

@Composable
private fun SagaHeader(
    session: UserSessionEntity,
    onOpenDailyReward: () -> Unit = {},
    canClaimDaily: Boolean = false,
    onOpenShop: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB7E4C7).copy(alpha = 0.95f),
                        Color(0xFFC7E9C0).copy(alpha = 0.90f),
                        Color.Transparent
                    )
                )
            )
            .displayCutoutPadding()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Player Profile Capsule with Golden Beveled Rim & LVL Ribbon
            PlayerProfileBadge(stage = session.currentStage)

            // CENTER: Candy Currency Pill (Swirl Candy + Gold Numbers + Emerald '+')
            CandyCurrencyPill(candies = session.candies, onClick = onOpenShop)

            // RIGHT: Hearts Capsule ("5/5" + Ruby Heart) & Bouncing Daily Gift Chest
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeartsPill(
                    lives = session.lives,
                    maxLives = session.maxLives,
                    onClick = onOpenShop
                )

                DailyGiftButton(
                    canClaim = canClaimDaily,
                    onClick = onOpenDailyReward
                )
            }
        }
    }
}

@Composable
private fun PlayerProfileBadge(stage: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 2.dp)
    ) {
        // 3D Beveled Golden Frame with Avatar
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFF2A3),
                            Color(0xFFF59E0B),
                            Color(0xFF92400E)
                        )
                    )
                )
                .padding(2.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE680), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🍓", fontSize = 24.sp)
            }
        }

        // Bold Gold Level Ribbon Badge
        Box(
            modifier = Modifier
                .offset(y = (-8).dp)
                .shadow(2.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFD166), Color(0xFFF59E0B))
                    )
                )
                .border(1.dp, Color(0xFFFFE899), RoundedCornerShape(10.dp))
                .padding(horizontal = 7.dp, vertical = 1.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LVL $stage",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF422006)
            )
        }
    }
}

@Composable
private fun CandyCurrencyPill(candies: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFDF8), Color(0xFFFFF3DC))
                )
            )
            .border(1.5.dp, Color(0xFFFFE8A3), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 3D Swirl Peppermint Icon
            Canvas(modifier = Modifier.size(18.dp)) {
                val r = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFF4D6D),
                            Color(0xFFFFFFFF),
                            Color(0xFFFF4D6D),
                            Color(0xFFFFFFFF),
                            Color(0xFFFF4D6D)
                        ),
                        center = center
                    ),
                    radius = r,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = r * 0.35f,
                    center = center
                )
            }

            Text(
                text = "$candies",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF592800)
            )

            // Mini Gold Plus
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFE494), Color(0xFFF59E0B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF592800),
                    modifier = Modifier.offset(y = (-1).dp)
                )
            }
        }
    }
}

@Composable
private fun HeartsPill(lives: Int, maxLives: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFDF8), Color(0xFFFFEDF1))
                )
            )
            .border(1.5.dp, Color(0xFFFFCCD5), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "❤️", fontSize = 13.sp)
            Text(
                text = "$lives/$maxLives",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF800F2F)
            )
            // Mini Plus
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFE494), Color(0xFFF59E0B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF592800),
                    modifier = Modifier.offset(y = (-1).dp)
                )
            }
        }
    }
}

@Composable
private fun DailyGiftButton(canClaim: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (canClaim) -4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .offset(y = bounceOffset.dp)
            .size(38.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFE380),
                        Color(0xFFFFB703),
                        Color(0xFFE07A00)
                    )
                )
            )
            .border(1.dp, Color(0xFFFFFBD6), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "🎁", fontSize = 20.sp)

        if (canClaim) {
            // Notification ping pip
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(10.dp)
                    .shadow(2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFE63946))
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}

/* =========================================================================
   3D WINDING COOKIE-WAFER PATH
   ========================================================================= */

@Composable
private fun ConnectingWaferPath(
    prevX: Dp?,
    currentX: Dp,
    nextX: Dp?,
    nodeCenterYDp: Dp? = null
) {
    val density = LocalDensity.current

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val halfW = w / 2f

        val curXpx = halfW + with(density) { currentX.toPx() }
        val curY = nodeCenterYDp?.let { with(density) { it.toPx() } } ?: (h / 2f)

        val prevXpx = prevX?.let { halfW + with(density) { it.toPx() } }
        val nextXpx = nextX?.let { halfW + with(density) { it.toPx() } }

        // Start point at top edge (y = 0 or -1f)
        val startX = if (prevXpx != null) (prevXpx + curXpx) / 2f else curXpx
        val startY = if (prevXpx != null) -1f else curY

        // End point at bottom edge (y = h or h + 1f)
        val endX = if (nextXpx != null) (curXpx + nextXpx) / 2f else curXpx
        val endY = if (nextXpx != null) (h + 1f) else curY

        // Tangent vector through current stage node
        val tmidX = when {
            prevXpx != null && nextXpx != null -> (nextXpx - prevXpx) * 0.35f
            prevXpx != null -> (curXpx - prevXpx) * 0.35f
            nextXpx != null -> (nextXpx - curXpx) * 0.35f
            else -> 0f
        }

        val path = Path()

        if (prevXpx != null && nextXpx != null) {
            // Full through-path: enter vertically from top boundary, curve through center, exit vertically at bottom
            path.moveTo(startX, startY)
            path.cubicTo(
                startX, startY + (curY - startY) * 0.45f,
                curXpx - tmidX, curY - (curY - startY) * 0.35f,
                curXpx, curY
            )
            path.cubicTo(
                curXpx + tmidX, curY + (endY - curY) * 0.35f,
                endX, endY - (endY - curY) * 0.45f,
                endX, endY
            )
        } else if (prevXpx != null) {
            // End of road (bottom-most level, e.g. Stage 1)
            path.moveTo(startX, startY)
            path.cubicTo(
                startX, startY + (curY - startY) * 0.45f,
                curXpx - tmidX, curY - (curY - startY) * 0.35f,
                curXpx, curY
            )
        } else if (nextXpx != null) {
            // Start of road (top-most level, e.g. Stage 50 summit)
            path.moveTo(curXpx, curY)
            path.cubicTo(
                curXpx + tmidX, curY + (endY - curY) * 0.35f,
                endX, endY - (endY - curY) * 0.45f,
                endX, endY
            )
        }

        val cap = if (prevXpx == null || nextXpx == null) StrokeCap.Round else StrokeCap.Butt

        // Layer 1: Ambient 3D Occlusion Shadow
        drawPath(
            path = path,
            color = Color(0x2814321E),
            style = Stroke(
                width = 46.dp.toPx(),
                cap = cap,
                join = StrokeJoin.Round
            )
        )

        // Layer 2: Cookie-Wafer Base Edge
        drawPath(
            path = path,
            color = Color(0xFFCBB49E),
            style = Stroke(
                width = 42.dp.toPx(),
                cap = cap,
                join = StrokeJoin.Round
            )
        )

        // Layer 3: Sugar Vanilla Frosted Road Surface
        drawPath(
            path = path,
            color = Color(0xFFFFF9EE),
            style = Stroke(
                width = 34.dp.toPx(),
                cap = cap,
                join = StrokeJoin.Round
            )
        )

        // Layer 4: Tactile Golden Stepping Cobblestones
        drawPath(
            path = path,
            color = Color(0xFFFFEAA7).copy(alpha = 0.90f),
            style = Stroke(
                width = 14.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 22f), 0f)
            )
        )

        // Layer 5: Sweet Strawberry & Soda Candy Sprinkles
        drawPath(
            path = path,
            color = Color(0xFFFF4D6D).copy(alpha = 0.70f),
            style = Stroke(
                width = 5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 44f), 8f)
            )
        )
        drawPath(
            path = path,
            color = Color(0xFF00B4D8).copy(alpha = 0.65f),
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 38f), 20f)
            )
        )
    }
}

/**
 * Playful roadside candy decorations (lollipops, gumdrops, flowers) along the journey.
 */
@Composable
private fun RoadsideCandyDecorations(stageNumber: Int) {
    val xOffset = calculateStageXOffset(stageNumber)
    // Place decorations on the opposite side of the road
    val decorationX = if (xOffset >= 0.dp) {
        (-120).dp + (sin(stageNumber * 1.5f) * 15f).dp
    } else {
        120.dp + (sin(stageNumber * 1.5f) * 15f).dp
    }

    Box(
        modifier = Modifier
            .offset(x = decorationX)
            .size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        when (stageNumber % 5) {
            0 -> Text(text = "🍭", fontSize = 22.sp)
            1 -> Text(text = "🌸", fontSize = 20.sp)
            2 -> Text(text = "🍬", fontSize = 20.sp)
            3 -> Text(text = "🍄", fontSize = 20.sp)
            4 -> Text(text = "✨", fontSize = 18.sp)
        }
    }
}

/* =========================================================================
   3D LEVEL NODES (COMPLETED, CURRENT HERO, LOCKED)
   ========================================================================= */

@Composable
private fun StageNode(
    stage: SagaStage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            stage.isCurrent -> CurrentHeroStageNode(stage = stage, onClick = onClick)
            stage.isUnlocked -> CompletedStageNode(stage = stage, onClick = onClick)
            else -> LockedStageNode(stage = stage)
        }
    }
}

/**
 * Completed Level: Glossy cyan/blue candy bevel button with gold rim and 3D star ribbon.
 */
@Composable
private fun CompletedStageNode(
    stage: SagaStage,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // 3D Candy Button
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                // 3D bottom bevel rim (#005F73)
                .background(Color(0xFF005F73))
                .border(2.5.dp, Color(0xFFFFE494), CircleShape),
            contentAlignment = Alignment.TopCenter
        ) {
            // Beveled Dome Face
            Box(
                modifier = Modifier
                    .size(52.dp, 48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF90E0EF), // Highlight
                                Color(0xFF00B4D8), // Mid cyan
                                Color(0xFF0077B6)  // Depth shade
                            ),
                            center = Offset(35f, 25f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Specular glaze highlight crescent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.80f), Color.Transparent)
                            )
                        )
                )

                Text(
                    text = "${stage.stageNumber}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF004052),
                            offset = Offset(0f, 2f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }

        // Cream Ribbon with Three 3D Embossed Gold Stars
        Box(
            modifier = Modifier
                .offset(y = (-6).dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFFDF5), Color(0xFFF7EEDD))
                    )
                )
                .border(1.dp, Color(0xFFE8DCC4), RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                for (i in 1..3) {
                    val isEarned = i <= stage.stars
                    Text(
                        text = "★",
                        fontSize = 11.sp,
                        color = if (isEarned) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                        style = TextStyle(
                            shadow = if (isEarned) Shadow(
                                color = Color(0xFFB45309),
                                offset = Offset(0f, 1f),
                                blurRadius = 1f
                            ) else null
                        )
                    )
                }
            }
        }
    }
}

/**
 * Current Active Level: Glowing sunburst halo, crowned Strawberry mascot,
 * golden honey-amber dome button, and extruded 3D "PLAY! ▶" red candy banner.
 */
@Composable
private fun CurrentHeroStageNode(
    stage: SagaStage,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val sunburstScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Glowing Sunburst Radial Halo behind dome
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(sunburstScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFEE440).copy(alpha = 0.65f),
                                Color(0xFFFB8500).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Crowned Mascot Pin (Strawberry Jelly)
                Box(
                    modifier = Modifier
                        .offset(y = (bounceOffset + 6).dp)
                        .size(42.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFF4B8), Color(0xFFF59E0B))
                            )
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = "👑", fontSize = 12.sp, lineHeight = 12.sp)
                            Text(
                                text = "🍓",
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.offset(y = (-3).dp)
                            )
                        }
                    }
                }

                // Plump Golden Honey-Amber 3D Dome Button
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        // 3D bottom bevel rim (#C2410C)
                        .background(Color(0xFFC2410C))
                        .border(3.dp, Color(0xFFFFE494), CircleShape),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp, 56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFF3B0),
                                        Color(0xFFFFB703),
                                        Color(0xFFFB8500)
                                    ),
                                    center = Offset(35f, 25f)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Specular Glaze
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 10.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.85f), Color.Transparent)
                                    )
                                )
                        )

                        Text(
                            text = "${stage.stageNumber}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF422006),
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.White.copy(alpha = 0.7f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 0f
                                )
                            )
                        )
                    }
                }
            }
        }

        // Extruded Juicy 3D "PLAY! ▶" Red Candy Action Pill Banner
        Box(
            modifier = Modifier
                .offset(y = (-4).dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                // 3D bevel rim (#670020)
                .background(Color(0xFF670020))
                .padding(bottom = 2.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFF5D8F),
                                Color(0xFFE63946),
                                Color(0xFFA80038)
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFFFFCCD5), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PLAY! ▶",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF670020),
                            offset = Offset(0f, 1.5f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }
    }
}

/**
 * Locked Level: Stone/chocolate disc with gold rim, centered level number, and cool 3D golden padlock perched on top.
 */
@Composable
private fun LockedStageNode(stage: SagaStage) {
    Box(
        contentAlignment = Alignment.TopCenter
    ) {
        // Main Stone / Chocolate Bevel Disc
        Box(
            modifier = Modifier
                .padding(top = 10.dp) // Generous room for the perched lock
                .size(50.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                // 3D bottom bevel rim (#5E5247)
                .background(Color(0xFF5E5247))
                .border(2.5.dp, Color(0xFFFFE494), CircleShape),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp, 45.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE5DDD3),
                                Color(0xFFB8A89A),
                                Color(0xFF877769)
                            ),
                            center = Offset(28f, 18f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Specular highlight ellipse at top
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawOval(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.65f), Color.Transparent)
                        ),
                        topLeft = Offset(size.width * 0.2f, 3f),
                        size = Size(size.width * 0.6f, size.height * 0.30f)
                    )
                }

                // Level Number in the CENTER!
                Text(
                    text = "${stage.stageNumber}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFFBF5),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF4A3B2C),
                            offset = Offset(0f, 2f),
                            blurRadius = 3f
                        )
                    )
                )
            }
        }

        // 3D Cool Golden Padlock perched on TOP
        Box(
            modifier = Modifier
                .offset(y = (-8).dp)
                .zIndex(2f)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFF4B8),
                            Color(0xFFFFD166),
                            Color(0xFFD97706)
                        )
                    )
                )
                .border(1.5.dp, Color(0xFFFFF9DB), CircleShape)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔒",
                fontSize = 11.sp
            )
        }
    }
}

/* =========================================================================
   RUSTIC CARVED WOODEN SIGNPOST (BIOME MILESTONE)
   ========================================================================= */

@Composable
private fun RusticWoodenSignpost(title: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // Carved wooden board
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                // 3D bottom wood rim (#451A03)
                .background(Color(0xFF451A03))
                .padding(bottom = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFB45309), // Warm amber wood
                                Color(0xFF854D0E), // Mid wood
                                Color(0xFF713F12)  // Deep mahogany
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFFD97706), RoundedCornerShape(11.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Left Brass Nail
                    Canvas(modifier = Modifier.size(5.dp)) {
                        drawCircle(color = Color(0xFFFDE68A))
                    }

                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFEF3C7),
                        letterSpacing = 0.5.sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xFF451A03),
                                offset = Offset(0f, 1.5f),
                                blurRadius = 2f
                            )
                        )
                    )

                    // Right Brass Nail
                    Canvas(modifier = Modifier.size(5.dp)) {
                        drawCircle(color = Color(0xFFFDE68A))
                    }
                }
            }
        }

        // Wooden Signpost Stake
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 12.dp)
                .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF713F12), Color(0xFF854D0E))
                    )
                )
        )
    }
}

@Composable
private fun CandyPeakSummit() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "🏰", fontSize = 32.sp)
        Text(
            text = "CANDY PEAK SUMMIT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF3B4A43).copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
    }
}

/* =========================================================================
   SCENERY DECORATIONS (CLOUDS, LOLLIPOPS, MARSHMALLOW BUSHES)
   ========================================================================= */

@Composable
private fun MapBackgroundScenery() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width

        // Fluffy Marshmallow Clouds in Upper Sky
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(w * 0.08f, 70.dp.toPx()),
            size = Size(100.dp.toPx(), 32.dp.toPx()),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.90f),
            radius = 24.dp.toPx(),
            center = Offset(w * 0.16f, 74.dp.toPx())
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.80f),
            topLeft = Offset(w * 0.72f, 100.dp.toPx()),
            size = Size(80.dp.toPx(), 28.dp.toPx()),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 20.dp.toPx(),
            center = Offset(w * 0.82f, 102.dp.toPx())
        )
    }
}


