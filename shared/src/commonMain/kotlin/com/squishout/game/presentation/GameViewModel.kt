package com.squishout.game.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import com.squishout.game.audio.SoundEffect
import com.squishout.game.board.LaunchAnimation
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.data.repository.GameRepository
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

    fun onTileTapped(pos: Position) {
        val result = engine.onTileTapped(pos)
        when (result) {
            is TapResult.Launched -> {
                audioPlayer?.playSound(SoundEffect.POP)
                audioPlayer?.triggerHaptic(isError = false)

                // Animate launch
                animateLaunch(result)

                if (result.isSolved) {
                    viewModelScope.launch {
                        audioPlayer?.playSound(SoundEffect.VICTORY)
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
                audioPlayer?.playSound(SoundEffect.WOBBLE)
                audioPlayer?.triggerHaptic(isError = true)
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

            val animatable = Animatable(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutLinearInEasing)
            ) {
                val currentProgress = value
                _activeLaunches.value = _activeLaunches.value.map {
                    if (it.jelly.id == result.jelly.id) it.copy(progress = currentProgress) else it
                }
            }

            // Remove finished launch animation
            _activeLaunches.value = _activeLaunches.value.filterNot { it.jelly.id == result.jelly.id }
        }
    }

    private fun animateWobble(jellyId: String) {
        viewModelScope.launch {
            val animatable = Animatable(0f)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200, easing = LinearEasing)
            ) {
                // 3 full sine waves
                val offset = sin(value * 6f * kotlin.math.PI.toFloat()) * (1f - value)
                _wobbleOffsets.value = _wobbleOffsets.value + (jellyId to offset)
            }
            _wobbleOffsets.value = _wobbleOffsets.value - jellyId
        }
    }

    fun useUndo() {
        if (engine.useUndo()) {
            audioPlayer?.playSound(SoundEffect.BOOSTER)
            audioPlayer?.triggerHaptic(isError = false)
        }
    }

    fun useHint() {
        val hintId = engine.useHint()
        if (hintId != null) {
            audioPlayer?.playSound(SoundEffect.BOOSTER)
            audioPlayer?.triggerHaptic(isError = false)
        }
    }

    fun useMagicWand() {
        val removed = engine.useMagicWand()
        if (removed != null) {
            audioPlayer?.playSound(SoundEffect.POP)
            audioPlayer?.triggerHaptic(isError = false)
        }
    }

    fun nextLevel() {
        loadStage(currentStage + 1)
    }

    fun restartLevel() {
        loadStage(currentStage)
    }
}
