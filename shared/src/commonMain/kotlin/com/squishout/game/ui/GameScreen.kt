package com.squishout.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.board.SkiaBoardView
import com.squishout.game.presentation.GameViewModel
import com.squishout.game.theme.SquishColors
import com.squishout.game.theme.SquishTypography

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val session by viewModel.sessionState.collectAsState()
    val activeLaunches by viewModel.activeLaunches.collectAsState()
    val wobbleOffsets by viewModel.wobbleOffsets.collectAsState()
    val biome = com.squishout.game.theme.BiomeTheme.forStage(gameState.stageNumber)

    var isSettingsOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var isShopOpen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = biome.backgroundTop
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Clean Top Header
                GameHeader(
                    stage = gameState.stageNumber,
                    hearts = gameState.hearts,
                    biome = biome,
                    onBack = onBackToMap,
                    onSettings = { isSettingsOpen = true }
                )

                // 2. The Hero 6x6 Board
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    SkiaBoardView(
                        board = gameState.board,
                        highlightedJellyId = gameState.highlightedJellyId,
                        stage = gameState.stageNumber,
                        activeLaunches = activeLaunches,
                        wobbleOffsets = wobbleOffsets,
                        onTileTapped = viewModel::onTileTapped
                    )
                }

                // 3. Ergonomic Bottom Booster Dock
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    BoosterDock(
                        undoCount = gameState.undoCount,
                        hintCount = gameState.hintCount,
                        wandCount = gameState.wandCount,
                        onUndo = viewModel::useUndo,
                        onHint = viewModel::useHint,
                        onWand = viewModel::useMagicWand,
                        onShopRequested = { isShopOpen = true }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 4. Level Victory Modal Overlay
            AnimatedVisibility(
                visible = gameState.isCompleted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                VictoryModal(
                    stars = gameState.starsEarned,
                    score = gameState.score,
                    stage = gameState.stageNumber,
                    onNextLevel = viewModel::nextLevel,
                    onReplay = viewModel::restartLevel
                )
            }

            // 5. Game Over Modal Overlay
            GameOverModal(
                isVisible = gameState.isGameOver,
                onReviveWithAd = {
                    viewModel.reviveWithAd()
                },
                onReviveWithGems = {
                    viewModel.purchaseHeartRefill()
                },
                onRestart = {
                    viewModel.restartLevel()
                }
            )

            // 6. Settings & Pause Modal Overlay
            SettingsModal(
                isVisible = isSettingsOpen,
                soundEnabled = session.soundEnabled,
                hapticsEnabled = session.hapticsEnabled,
                onToggleSound = viewModel::toggleSound,
                onToggleHaptics = viewModel::toggleHaptics,
                onResume = { isSettingsOpen = false },
                onRestart = {
                    isSettingsOpen = false
                    viewModel.restartLevel()
                },
                onExitToMap = {
                    isSettingsOpen = false
                    onBackToMap()
                }
            )

            // 7. Booster Shop Modal Overlay
            BoosterShopModal(
                isVisible = isShopOpen,
                candies = session.candies,
                gems = session.gems,
                onBuyUndo = { viewModel.purchaseUndoPack() },
                onBuyHint = { viewModel.purchaseHintPack() },
                onBuyWand = { viewModel.purchaseWandPack() },
                onRefillHeartsGems = { viewModel.purchaseHeartRefill() },
                onRefillHeartAd = { viewModel.reviveWithAd() },
                onDismiss = { isShopOpen = false }
            )
        }
    }
}

@Composable
private fun GameHeader(
    stage: Int,
    hearts: Int,
    biome: com.squishout.game.theme.BiomeTheme = com.squishout.game.theme.BiomeTheme.SWEET_MEADOW,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back / Pause button
        HeaderIconButton(icon = "←", onClick = onBack)

        // Center Stage & Hearts
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${biome.icon} Stage $stage",
                    style = SquishTypography.titleMedium,
                    color = biome.accentText,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3 Hearts
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..3) {
                    Text(
                        text = if (i <= hearts) "❤️" else "🤍",
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Settings gear button
        HeaderIconButton(icon = "⚙", onClick = onSettings)
    }
}

@Composable
private fun HeaderIconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 18.sp,
            color = SquishColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}
