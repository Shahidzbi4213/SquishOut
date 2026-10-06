package com.squishout.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.squishout.engine.model.EyeState
import com.squishout.game.board.SkiaBoardView
import com.squishout.game.presentation.GameViewModel
import com.squishout.game.theme.BiomeTheme
import com.squishout.game.ui.components.AwakeStatusChip
import com.squishout.game.ui.components.CandyHeartsView
import com.squishout.game.ui.components.GameIconType
import com.squishout.game.ui.components.GameTactileButton
import com.squishout.game.ui.components.StageHeaderPlaque

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    val session by viewModel.sessionState.collectAsState()
    val biome = BiomeTheme.forStage(gameState.stageNumber)

    var isSettingsOpen by remember { mutableStateOf(false) }
    var isShopOpen by remember { mutableStateOf(false) }

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
                // 1. Game Arcade Top Header
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
                        activeLaunchesFlow = viewModel.activeLaunches,
                        wobbleOffsetsFlow = viewModel.wobbleOffsets,
                        blockerRecoilsFlow = viewModel.blockerRecoils,
                        flyingRewardsFlow = viewModel.flyingRewards,
                        shatteredObstaclesFlow = viewModel.shatteredObstacles,
                        onTileTapped = viewModel::onTileTapped
                    )
                }

                // 3. Ergonomic Bottom Booster Dock & Awake Status Chip
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    val awakeCount = remember(gameState.board.jellies) {
                        gameState.board.jellies.count { it.eyeState == EyeState.AWAKE }
                    }
                    AwakeStatusChip(awakeCount = awakeCount)
                    Spacer(modifier = Modifier.height(10.dp))
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
                },
                stage = gameState.stageNumber,
                hearts = gameState.hearts
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
    biome: BiomeTheme = BiomeTheme.SWEET_MEADOW,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // Center Golden-Wood Plaque & 3D Candy Hearts
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StageHeaderPlaque(
                stage = stage,
                biome = biome
            )
            CandyHeartsView(
                hearts = hearts,
                maxHearts = 3
            )
        }

        // Back / Pause 3D tactile button (aligned with StageHeaderPlaque center)
        GameTactileButton(
            iconType = GameIconType.BACK,
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 2.dp)
        )

        // Settings 3D tactile gear button (aligned with StageHeaderPlaque center)
        GameTactileButton(
            iconType = GameIconType.SETTINGS,
            onClick = onSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp)
        )
    }
}
