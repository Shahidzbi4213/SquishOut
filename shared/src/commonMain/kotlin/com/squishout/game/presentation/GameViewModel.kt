package com.squishout.game.presentation

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squishout.engine.GameEngine
import com.squishout.engine.GameState
import com.squishout.engine.TapResult
import com.squishout.engine.generator.LevelConfig
import com.squishout.engine.generator.ReverseAssemblyGenerator
import com.squishout.engine.model.Position
import com.squishout.game.audio.AudioPlayer
import com.squishout.game.audio.HapticFeedbackType
import com.squishout.game.audio.SoundEffect
import com.squishout.game.board.LaunchAnimation
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.sin

class GameViewModel(
    private val engine: GameEngine = GameEngine(),
    private val generator: ReverseAssemblyGenerator = ReverseAssemblyGenerator(),
    private val audioPlayer: AudioPlayer? = null,
    private val repository: GameRepository? = null
) : ViewModel() {

    val gameState: StateFlow<GameState> = engine.state
    val sessionState: StateFlow<UserSessionEntity> = (repository?.session ?: MutableStateFlow(UserSessionEntity()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserSessionEntity()
        )

    private val _activeLaunches = MutableStateFlow<List<LaunchAnimation>>(emptyList())
    val activeLaunches: StateFlow<List<LaunchAnimation>> = _activeLaunches.asStateFlow()

    private val _wobbleOffsets = MutableStateFlow<Map<String, Float>>(emptyMap())
    val wobbleOffsets: StateFlow<Map<String, Float>> = _wobbleOffsets.asStateFlow()

    private var currentStage = 1

    init {
        loadStage(currentStage)
    }

    fun loadStage(stage: Int) {
        currentStage = stage
        val config = LevelConfig(
            stageNumber = stage,
            jellyCount = (10 + (stage - 1) * 2).coerceAtMost(22),
            obstacleCount = ((stage - 1) / 3).coerceAtMost(3),
            includeMultiCell = stage >= 3,
            seed = stage * 1000L + 42L
        )
        val level = generator.generate(config)
        engine.loadLevel(level)
        _activeLaunches.value = emptyList()
        _wobbleOffsets.value = emptyMap()
    }

    private fun playSound(sound: SoundEffect) {
        if (sessionState.value.soundEnabled) {
            audioPlayer?.playSound(sound)
        }
    }

    private fun triggerHaptic(type: HapticFeedbackType) {
        if (sessionState.value.hapticsEnabled) {
            audioPlayer?.triggerHaptic(type)
        }
    }

    private fun triggerHaptic(isError: Boolean = false) {
        triggerHaptic(if (isError) HapticFeedbackType.ERROR_WOBBLE else HapticFeedbackType.LIGHT_CLICK)
    }

    fun onTileTapped(pos: Position) {
        val result = engine.onTileTapped(pos)
        when (result) {
            is TapResult.Launched -> {
                if (result.shatteredObstacles.isNotEmpty()) {
                    playSound(SoundEffect.CRACK)
                    triggerHaptic(HapticFeedbackType.CRACK_THUMP)
                } else {
                    playSound(SoundEffect.POP)
                    triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
                }

                // Animate launch
                animateLaunch(result)

                if (result.isSolved) {
                    viewModelScope.launch {
                        delay(200) // Brief dramatic pause for launch animation
                        playSound(SoundEffect.VICTORY)
                        triggerHaptic(HapticFeedbackType.VICTORY_FANFARE)
                        val finalState = engine.state.value
                        repository?.recordLevelCompletion(
                            levelNumber = currentStage,
                            stars = result.stars,
                            score = finalState.score,
                            movesUsed = finalState.movesUsed,
                            timestampEpoch = 0L
                        )
                    }
                }
            }
            is TapResult.Blocked -> {
                playSound(SoundEffect.WOBBLE)
                triggerHaptic(HapticFeedbackType.ERROR_WOBBLE)
                animateWobble(result.jelly.id)
            }
            TapResult.EmptyTile, TapResult.GameOver -> {
                // No action
            }
        }
    }

    private fun animateLaunch(result: TapResult.Launched) {
        viewModelScope.launch {
            val tileSize = 60f // Logical unit
            val minX = result.jelly.tiles.minOf { it.x }
            val minY = result.jelly.tiles.minOf { it.y }
            val start = Offset(minX * tileSize, minY * tileSize)
            val distance = tileSize * 8f // Fly well off board

            val anim = LaunchAnimation(
                jelly = result.jelly,
                startOffset = start,
                exitDirection = result.jelly.direction,
                distance = distance,
                progress = 0f
            )

            _activeLaunches.value = _activeLaunches.value + anim

            val durationMs = 320L
            val startTime = com.squishout.game.util.currentTimeMillis()
            while (true) {
                val elapsed = com.squishout.game.util.currentTimeMillis() - startTime
                val rawProgress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                // FastOutLinearIn easing: t^2
                val progress = rawProgress * rawProgress
                _activeLaunches.value = _activeLaunches.value.map {
                    if (it.jelly.id == result.jelly.id) it.copy(progress = progress) else it
                }
                if (rawProgress >= 1f) break
                delay(16)
            }

            // Remove finished launch animation
            _activeLaunches.value = _activeLaunches.value.filterNot { it.jelly.id == result.jelly.id }
        }
    }

    private fun animateWobble(jellyId: String) {
        viewModelScope.launch {
            val durationMs = 200L
            val startTime = com.squishout.game.util.currentTimeMillis()
            while (true) {
                val elapsed = com.squishout.game.util.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                // 3 full sine waves with decay
                val offset = sin(progress * 6f * kotlin.math.PI.toFloat()) * (1f - progress)
                _wobbleOffsets.value = _wobbleOffsets.value + (jellyId to offset)
                if (progress >= 1f) break
                delay(16)
            }
            _wobbleOffsets.value = _wobbleOffsets.value - jellyId
        }
    }

    fun useUndo() {
        if (engine.useUndo()) {
            playSound(SoundEffect.BOOSTER)
            triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
        }
    }

    fun useHint() {
        val hintId = engine.useHint()
        if (hintId != null) {
            playSound(SoundEffect.BOOSTER)
            triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
        }
    }

    fun useMagicWand() {
        val removed = engine.useMagicWand()
        if (removed != null) {
            playSound(SoundEffect.POP)
            triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
        }
    }

    fun nextLevel() {
        loadStage(currentStage + 1)
    }

    fun restartLevel() {
        loadStage(currentStage)
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            repository?.toggleSound(enabled)
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch {
            repository?.toggleHaptics(enabled)
        }
    }

    fun purchaseUndoPack(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (repository?.spendCandies(50) == true) {
                engine.addBoosters(undo = 3)
                playSound(SoundEffect.BOOSTER)
                triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
                onSuccess()
            }
        }
    }

    fun purchaseHintPack(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (repository?.spendCandies(75) == true) {
                engine.addBoosters(hint = 3)
                playSound(SoundEffect.BOOSTER)
                triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
                onSuccess()
            }
        }
    }

    fun purchaseWandPack(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (repository?.spendCandies(100) == true) {
                engine.addBoosters(wand = 1)
                playSound(SoundEffect.BOOSTER)
                triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
                onSuccess()
            }
        }
    }

    fun purchaseHeartRefill(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (repository?.reviveWithGems(10) == true) {
                engine.refillHearts()
                playSound(SoundEffect.BOOSTER)
                triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
                onSuccess()
            }
        }
    }

    fun reviveWithAd(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (repository?.reviveWithAd() == true) {
                engine.refillHearts()
                playSound(SoundEffect.BOOSTER)
                triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
                onSuccess()
            }
        }
    }

    fun buyBoosters(costCandies: Int, onPurchased: () -> Unit) {
        viewModelScope.launch {
            if (repository?.spendCandies(costCandies) == true) {
                onPurchased()
            }
        }
    }
}
