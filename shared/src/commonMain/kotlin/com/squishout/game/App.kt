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
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.data.repository.GameRepository
import com.squishout.game.presentation.GameViewModel
import com.squishout.game.theme.SquishOutTheme
import com.squishout.game.ui.DailyPuzzleCalendarScreen
import com.squishout.game.ui.GameScreen
import com.squishout.game.ui.JellyDexScreen
import com.squishout.game.ui.SagaMapScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class AppScreen {
    SAGA_MAP,
    GAMEPLAY,
    JELLY_DEX,
    DAILY_CALENDAR
}

@Composable
@Preview
fun App() {
    val repository = koinInject<GameRepository>()
    val viewModel = koinViewModel<GameViewModel>()
    val scope = rememberCoroutineScope()

    LifecycleResumeEffect(Unit) {
        viewModel.resumeAllAudio()
        onPauseOrDispose {
            viewModel.pauseAllAudio()
        }
    }

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

    com.squishout.game.util.PlatformBackHandler(enabled = currentScreen != AppScreen.SAGA_MAP) {
        currentScreen = AppScreen.SAGA_MAP
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
                            canClaimDaily = canClaimDaily,
                            onClaimStarChest = { milestone ->
                                val totalStars = levels.sumOf { it.stars }
                                viewModel.claimStarChest(milestone, totalStars)
                            },
                            onNavigateToCalendar = {
                                currentScreen = AppScreen.DAILY_CALENDAR
                            }
                        )
                    }

                    AppScreen.GAMEPLAY -> {
                        GameScreen(
                            viewModel = viewModel,
                            onBackToMap = {
                                currentScreen = if (viewModel.isDailyPuzzleMode) AppScreen.DAILY_CALENDAR else AppScreen.SAGA_MAP
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

                    AppScreen.DAILY_CALENDAR -> {
                        DailyPuzzleCalendarScreen(
                            session = session,
                            repository = repository,
                            onPlayPuzzle = { epochDay, dayOfMonth, dateString, monthKey ->
                                viewModel.loadDailyPuzzle(epochDay, dayOfMonth, dateString, monthKey)
                                currentScreen = AppScreen.GAMEPLAY
                            },
                            onBack = {
                                currentScreen = AppScreen.SAGA_MAP
                            },
                            onClaimMilestone = { monthKey, milestoneDays, completions ->
                                viewModel.claimMonthlyMilestone(monthKey, milestoneDays, completions)
                            }
                        )
                    }
                }
            }

            // Global Booster Shop Modal for Map & Dex
            com.squishout.game.ui.BoosterShopModal(
                isVisible = isShopOpen,
                diamonds = session.diamonds,
                onBuyUndo = { viewModel.purchaseUndoPack() },
                onBuyHint = { viewModel.purchaseHintPack() },
                onBuyWand = { viewModel.purchaseWandPack() },
                onRefillHeartsDiamonds = { viewModel.purchaseHeartRefill() },
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