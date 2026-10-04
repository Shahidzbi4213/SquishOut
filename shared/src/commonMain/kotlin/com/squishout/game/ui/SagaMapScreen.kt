package com.squishout.game.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.data.entity.LevelRecordEntity
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.theme.AmberGlow
import com.squishout.game.theme.EmeraldMint
import com.squishout.game.theme.FrostedWhite
import com.squishout.game.theme.SlateCharcoal
import com.squishout.game.theme.StrawberryBase
import kotlin.math.sin

data class SagaStage(
    val stageNumber: Int,
    val isUnlocked: Boolean,
    val isCurrent: Boolean,
    val stars: Int,
    val biome: String? = null
)

@Composable
fun SagaMapScreen(
    session: UserSessionEntity,
    levelRecords: List<LevelRecordEntity>,
    onSelectStage: (Int) -> Unit,
    onNavigateToDex: () -> Unit,
    onNavigateToShop: () -> Unit = {},
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
            1 -> "🌸 SWEET MEADOW (LVL 1-20)"
            21 -> "🌊 SODA LAGOON (LVL 21-40)"
            41 -> "🍯 HONEYCOMB VALLEY (LVL 41+)"
            else -> null
        }
        SagaStage(stageNum, isUnlocked, isCurrent, stars, biome)
    }.reversed() // Reverse so lower levels start at bottom, scroll upwards like classic saga

    val listState = rememberLazyListState()

    // Scroll to current stage on load
    LaunchedEffect(currentUnlocked) {
        val index = (totalStages - currentUnlocked).coerceIn(0, totalStages - 1)
        listState.scrollToItem(index)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE0F2FE), // Soda Lagoon Sky
                        Color(0xFFE8F5E9), // Meadow Green
                        Color(0xFFFFF7ED)  // Honey Valley Warmth
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            SagaHeader(session = session)

            // Scrollable Map
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "🏰 CANDY PEAK (FINALE)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateCharcoal.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(stages) { stage ->
                    // Biome Banner
                    if (stage.biome != null) {
                        BiomeBanner(title = stage.biome)
                    }

                    // Winding sinusoidal path offset
                    val xOffset = (sin(stage.stageNumber * 0.9f) * 75f).dp

                    StageNode(
                        stage = stage,
                        onClick = {
                            if (stage.isUnlocked) {
                                onSelectStage(stage.stageNumber)
                            }
                        },
                        modifier = Modifier
                            .offset(x = xOffset)
                            .padding(vertical = 14.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            // Bottom Navigation Bar
            SagaBottomNav(
                selectedTab = "map",
                onMapClick = {},
                onDexClick = onNavigateToDex,
                onShopClick = onNavigateToShop
            )
        }
    }
}

@Composable
private fun SagaHeader(session: UserSessionEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // App Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(StrawberryBase),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🍓", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SQUISH OUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = SlateCharcoal.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Jelly Garden",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateCharcoal
                    )
                }
            }

            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F766E)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Resource Pill Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Level Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(FrostedWhite)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "LVL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = EmeraldMint)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${session.currentStage}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateCharcoal)
            }

            // Candies Capsule
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FrostedWhite)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "🍬", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${session.candies}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SlateCharcoal)
            }

            // Lives Capsule
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(FrostedWhite)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "❤️", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${session.lives}/${session.maxLives}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateCharcoal
                )
            }
        }
    }
}

@Composable
private fun BiomeBanner(title: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedWhite.copy(alpha = 0.9f)),
        modifier = Modifier
            .padding(vertical = 12.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = SlateCharcoal,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StageNode(
    stage: SagaStage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (stage.isCurrent) {
            // Crown Mascot Pin on Current Stage
            Text(
                text = "👑🍓",
                fontSize = 20.sp,
                modifier = Modifier.offset(y = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(if (stage.isCurrent) 64.dp else 52.dp)
                .shadow(
                    elevation = if (stage.isCurrent) 12.dp else 4.dp,
                    shape = CircleShape,
                    spotColor = if (stage.isCurrent) AmberGlow else Color.Black.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    when {
                        stage.isCurrent -> Brush.radialGradient(
                            listOf(Color(0xFFFDE047), Color(0xFFF59E0B))
                        )
                        stage.isUnlocked -> Brush.radialGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                        )
                        else -> Brush.radialGradient(
                            listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                        )
                    }
                )
                .clickable(enabled = stage.isUnlocked) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (stage.isUnlocked) {
                Text(
                    text = "${stage.stageNumber}",
                    fontSize = if (stage.isCurrent) 22.sp else 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            } else {
                Text(text = "🔒", fontSize = 16.sp)
            }
        }

        if (stage.isCurrent) {
            // Bouncing PLAY! CTA Capsule
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                        )
                    )
                    .clickable { onClick() }
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PLAY! ▶",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        } else if (stage.isUnlocked && stage.stars > 0) {
            // Star rating display beneath stage
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                repeat(3) { index ->
                    Text(
                        text = if (index < stage.stars) "⭐" else "☆",
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SagaBottomNav(
    selectedTab: String,
    onMapClick: () -> Unit,
    onDexClick: () -> Unit,
    onShopClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedWhite),
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Map / Play Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onMapClick() }
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(text = "🗺️", fontSize = 20.sp)
                Text(
                    text = "Play",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == "map") FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (selectedTab == "map") EmeraldMint else SlateCharcoal.copy(alpha = 0.5f)
                )
            }

            // Jelly Dex Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDexClick() }
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(text = "🐾", fontSize = 20.sp)
                Text(
                    text = "Jellies",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == "dex") FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (selectedTab == "dex") EmeraldMint else SlateCharcoal.copy(alpha = 0.5f)
                )
            }

            // Shop Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onShopClick() }
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(text = "🍬", fontSize = 20.sp)
                Text(
                    text = "Shop",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == "shop") FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (selectedTab == "shop") EmeraldMint else SlateCharcoal.copy(alpha = 0.5f)
                )
            }
        }
    }
}
