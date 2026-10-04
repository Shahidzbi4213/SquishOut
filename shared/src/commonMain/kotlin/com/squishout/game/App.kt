package com.squishout.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.data.repository.GameRepository
import com.squishout.game.presentation.GameViewModel
import com.squishout.game.theme.SquishOutTheme
import com.squishout.game.ui.GameScreen
import com.squishout.game.ui.JellyDexScreen
import com.squishout.game.ui.SagaMapScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class AppScreen {
    SAGA_MAP,
    GAMEPLAY,
    JELLY_DEX
}

@Composable
@Preview
fun App() {
    val repository = koinInject<GameRepository>()
    val viewModel = koinViewModel<GameViewModel>()
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(AppScreen.SAGA_MAP) }
    var isShopOpen by remember { mutableStateOf(false) }
    var isDailyRewardOpen by remember { mutableStateOf(false) }

    val session by repository.session.collectAsState()
    val levels by repository.allLevels.collectAsState(initial = emptyList())
    val skins by repository.allSkins.collectAsState(initial = emptyList())

    val now = com.squishout.game.util.currentTimeMillis()
    val canClaimDaily = session.lastClaimEpochDay != (now / (24 * 60 * 60 * 1000L))

    androidx.compose.runtime.LaunchedEffect(canClaimDaily) {
        if (canClaimDaily) {
            isDailyRewardOpen = true
        }
    }

    SquishOutTheme {
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { screen ->
                when (screen) {
                    AppScreen.SAGA_MAP -> {
                        SagaMapScreen(
                            session = session,
                            levelRecords = levels,
                            onSelectStage = { stage ->
                                viewModel.loadStage(stage)
                                currentScreen = AppScreen.GAMEPLAY
                            },
                            onNavigateToDex = {
                                currentScreen = AppScreen.JELLY_DEX
                            },
                            onNavigateToShop = {
                                isShopOpen = true
                            },
                            onOpenDailyReward = {
                                isDailyRewardOpen = true
                            },
                            canClaimDaily = canClaimDaily
                        )
                    }

                    AppScreen.GAMEPLAY -> {
                        GameScreen(
                            viewModel = viewModel,
                            onBackToMap = {
                                currentScreen = AppScreen.SAGA_MAP
                            }
                        )
                    }

                    AppScreen.JELLY_DEX -> {
                        JellyDexScreen(
                            session = session,
                            skins = skins,
                            onEquipSkin = { skinId ->
                                scope.launch {
                                    repository.equipSkin(skinId)
                                }
                            },
                            onUnlockSkin = { skinId, cost ->
                                scope.launch {
                                    repository.unlockSkin(skinId, cost)
                                }
                            },
                            onNavigateToMap = {
                                currentScreen = AppScreen.SAGA_MAP
                            },
                            onNavigateToShop = {
                                isShopOpen = true
                            }
                        )
                    }
                }
            }

            // Global Booster Shop Modal for Map & Dex
            com.squishout.game.ui.BoosterShopModal(
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

            // Daily Streak Rewards Modal
            com.squishout.game.ui.DailyRewardModal(
                isVisible = isDailyRewardOpen,
                currentStreakDay = session.loginStreakDays,
                canClaimToday = canClaimDaily,
                onClaimReward = {
                    scope.launch {
                        val reward = repository.claimDailyReward(com.squishout.game.util.currentTimeMillis())
                        if (reward != null) {
                            isDailyRewardOpen = false
                        }
                    }
                },
                onDismiss = { isDailyRewardOpen = false }
            )
        }
    }
}