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
import com.squishout.game.board.FlyingRewardToken
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

    private val _blockerRecoils = MutableStateFlow<Map<Position, Float>>(emptyMap())
    val blockerRecoils: StateFlow<Map<Position, Float>> = _blockerRecoils.asStateFlow()

    private val _flyingRewards = MutableStateFlow<List<FlyingRewardToken>>(emptyList())
    val flyingRewards: StateFlow<List<FlyingRewardToken>> = _flyingRewards.asStateFlow()

    private val _shatteredObstacles = MutableStateFlow<List<com.squishout.engine.model.Obstacle>>(emptyList())
    val shatteredObstacles: StateFlow<List<com.squishout.engine.model.Obstacle>> = _shatteredObstacles.asStateFlow()

    private val _comboCount = MutableStateFlow(0)
    val comboCount: StateFlow<Int> = _comboCount.asStateFlow()

    private val _comboCallout = MutableStateFlow<String?>(null)
    val comboCallout: StateFlow<String?> = _comboCallout.asStateFlow()

    private val _currentDifficultyTier = MutableStateFlow(com.squishout.engine.model.DifficultyTier.NORMAL)
    val currentDifficultyTier: StateFlow<com.squishout.engine.model.DifficultyTier> = _currentDifficultyTier.asStateFlow()

    private var lastLaunchTimeMs = 0L
    private var currentStage = 1

    init {
        loadStage(currentStage)
        viewModelScope.launch {
            sessionState.collect { session ->
                audioPlayer?.setSoundEnabled(session.soundEnabled)
                audioPlayer?.setMusicEnabled(session.musicEnabled)
            }
        }
    }

    fun loadStage(stage: Int) {
        currentStage = stage
        val tier = com.squishout.engine.model.DifficultyTier.forStage(stage)
        _currentDifficultyTier.value = tier

        val gridSize = when {
            stage <= 15 -> 6
            stage <= 45 -> 7
            else -> 8
        }

        // Base jelly density according to grid size and progression
        val baseJellies = when {
            stage <= 5 -> 6 + stage * 2 // 8 to 16
            stage <= 15 -> 14 + (stage - 5) // 15 to 24
            stage <= 45 -> 20 + ((stage - 15) * 0.4).toInt() // 20 to 32
            else -> 28 + ((stage - 45) * 0.1).toInt().coerceAtMost(10) // 28 to 38
        }

        val adjustedJellies = when (tier) {
            com.squishout.engine.model.DifficultyTier.SUPER_HARD -> (baseJellies + 3).coerceAtMost(if (gridSize == 6) 22 else 38)
            com.squishout.engine.model.DifficultyTier.HARD -> (baseJellies + 2).coerceAtMost(if (gridSize == 6) 22 else 36)
            com.squishout.engine.model.DifficultyTier.BREATHER -> (baseJellies - 3).coerceAtLeast(8)
            com.squishout.engine.model.DifficultyTier.NORMAL -> baseJellies
        }

        val baseObstacles = when {
            stage <= 3 -> 0
            stage <= 10 -> 1
            stage <= 25 -> 2
            stage <= 50 -> 3
            stage <= 90 -> 4
            else -> 5
        }
        val adjustedObstacles = if (tier.isChallenging) baseObstacles + 1 else baseObstacles

        val maxInitialAwake = when (tier) {
            com.squishout.engine.model.DifficultyTier.SUPER_HARD -> 2
            com.squishout.engine.model.DifficultyTier.HARD -> 3
            com.squishout.engine.model.DifficultyTier.NORMAL -> 5
            com.squishout.engine.model.DifficultyTier.BREATHER -> 8
        }

        val config = LevelConfig(
            stageNumber = stage,
            gridWidth = gridSize,
            gridHeight = gridSize,
            jellyCount = adjustedJellies,
            obstacleCount = adjustedObstacles,
            maxInitialAwake = maxInitialAwake,
            difficultyTier = tier,
            includeMultiCell = stage >= 3,
            includeWaterJets = stage >= 5,
            includeSchoolingPairs = stage >= 7,
            includeBubbleFog = stage >= 12,
            includeKingJelly = tier == com.squishout.engine.model.DifficultyTier.SUPER_HARD || (stage >= 20 && stage % 5 == 0),
            seed = stage * 1000L + 42L
        )
        val level = generator.generate(config)
        engine.loadLevel(level)
        _activeLaunches.value = emptyList()
        _wobbleOffsets.value = emptyMap()
        _blockerRecoils.value = emptyMap()
        _flyingRewards.value = emptyList()
        _shatteredObstacles.value = emptyList()
        _comboCount.value = 0
        _comboCallout.value = null
        lastLaunchTimeMs = 0L
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
                val now = com.squishout.game.util.currentTimeMillis()
                val isCombo = (now - lastLaunchTimeMs) <= 1400L && lastLaunchTimeMs > 0L
                lastLaunchTimeMs = now

                val currentCombo = if (isCombo) (_comboCount.value + 1).coerceAtMost(5) else 1
                _comboCount.value = currentCombo

                if (result.shatteredObstacles.isNotEmpty()) {
                    playSound(SoundEffect.CRACK)
                    triggerHaptic(HapticFeedbackType.CRACK_THUMP)
                } else if (result.jelly.isBoss) {
                    playSound(SoundEffect.VICTORY)
                    triggerHaptic(HapticFeedbackType.VICTORY_FANFARE)
                } else if (result.partnerJelly != null) {
                    playSound(SoundEffect.BOOSTER)
                    triggerHaptic(HapticFeedbackType.VICTORY_FANFARE)
                } else if (currentCombo >= 2) {
                    playSound(SoundEffect.COMBO)
                    triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
                } else {
                    playSound(SoundEffect.POP)
                    triggerHaptic(HapticFeedbackType.LIGHT_CLICK)
                }

                if (currentCombo >= 2) {
                    val callout = when (currentCombo) {
                        2 -> "Sweet! 2x"
                        3 -> "Juicy! 3x"
                        4 -> "Super! 4x"
                        else -> "Squish-tastic! 5x"
                    }
                    _comboCallout.value = callout
                    viewModelScope.launch {
                        delay(1200)
                        if (_comboCallout.value == callout) {
                            _comboCallout.value = null
                        }
                    }
                }

                // Animate launch (handles single or symbiotic partner pair)
                animateLaunch(result)

                if (result.isSolved) {
                    viewModelScope.launch {
                        delay(250) // Let final jelly slow-motion climax start
                        // Spawn flying reward stars toward top HUD
                        val tokens = (0 until 5).map { i ->
                            FlyingRewardToken(
                                id = i,
                                startX = 240f,
                                startY = 240f,
                                targetX = 240f + (i - 2) * 20f,
                                targetY = -40f,
                                progress = 0f,
                                arcSpread = (i - 2) * 55f
                            )
                        }
                        _flyingRewards.value = tokens
                        val flyDurationMs = 520L
                        val flyStartTime = com.squishout.game.util.currentTimeMillis()
                        while (true) {
                            val elapsed = com.squishout.game.util.currentTimeMillis() - flyStartTime
                            val p = (elapsed.toFloat() / flyDurationMs).coerceIn(0f, 1f)
                            _flyingRewards.value = tokens.map { it.copy(progress = p) }
                            if (p >= 1f) break
                            delay(16)
                        }
                        _flyingRewards.value = emptyList()

                        playSound(SoundEffect.VICTORY)
                        triggerHaptic(HapticFeedbackType.VICTORY_FANFARE)
                        val finalState = engine.state.value
                        repository?.recordLevelCompletion(
                            levelNumber = currentStage,
                            stars = result.stars,
                            score = finalState.score,
                            movesUsed = finalState.movesUsed,
                            timestampEpoch = 0L,
                            difficultyTier = _currentDifficultyTier.value
                        )
                    }
                }
            }
            is TapResult.Blocked -> {
                _comboCount.value = 0
                _comboCallout.value = null
                playSound(SoundEffect.WOBBLE)
                triggerHaptic(HapticFeedbackType.ERROR_WOBBLE)
                animateWobble(result.jelly.id, result.blockerPosition)
            }
            TapResult.EmptyTile, TapResult.GameOver -> {
                // No action
            }
        }
    }

    private fun animateLaunch(result: TapResult.Launched) {
        viewModelScope.launch {
            val isClimax = result.isSolved
            val anim1 = LaunchAnimation(
                jelly = result.jelly,
                exitPath = result.exitPath,
                progress = 0f,
                isFeverClimax = isClimax
            )
            val partner = result.partnerJelly
            val partnerExit = result.partnerExitPath
            val partnerAnim = if (partner != null && partnerExit != null) {
                LaunchAnimation(
                    jelly = partner,
                    exitPath = partnerExit,
                    progress = 0f,
                    isFeverClimax = isClimax
                )
            } else null

            if (result.shatteredObstacles.isNotEmpty()) {
                _shatteredObstacles.value = result.shatteredObstacles
            }

            val launchingIds = setOfNotNull(result.jelly.id, result.partnerJelly?.id)
            val newAnims = listOfNotNull(anim1, partnerAnim)
            _activeLaunches.value = _activeLaunches.value + newAnims

            // Final squishy gets dramatic slow-motion time dilation
            val durationMs = if (isClimax) 560L else 340L
            val startTime = com.squishout.game.util.currentTimeMillis()
            while (true) {
                val elapsed = com.squishout.game.util.currentTimeMillis() - startTime
                val rawProgress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                val progress = rawProgress * rawProgress
                _activeLaunches.value = _activeLaunches.value.map {
                    if (it.jelly.id in launchingIds) it.copy(progress = progress) else it
                }
                if (rawProgress >= 1f) break
                delay(16)
            }

            // Remove finished launch animation
            _activeLaunches.value = _activeLaunches.value.filterNot { it.jelly.id in launchingIds }
        }
    }

    private fun animateWobble(jellyId: String, blockerPosition: Position? = null) {
        viewModelScope.launch {
            val durationMs = 220L
            val startTime = com.squishout.game.util.currentTimeMillis()
            while (true) {
                val elapsed = com.squishout.game.util.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                // 3 full sine waves with decay
                val offset = sin(progress * 6f * kotlin.math.PI.toFloat()) * (1f - progress)
                _wobbleOffsets.value = _wobbleOffsets.value + (jellyId to offset)

                if (blockerPosition != null) {
                    // Blocker impact recoil shudder
                    val recoil = sin(progress * 8f * kotlin.math.PI.toFloat()) * (1f - progress) * 0.75f
                    _blockerRecoils.value = _blockerRecoils.value + (blockerPosition to recoil)
                }

                if (progress >= 1f) break
                delay(16)
            }
            _wobbleOffsets.value = _wobbleOffsets.value - jellyId
            if (blockerPosition != null) {
                _blockerRecoils.value = _blockerRecoils.value - blockerPosition
            }
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
            audioPlayer?.setSoundEnabled(enabled)
        }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch {
            repository?.toggleMusic(enabled)
            audioPlayer?.setMusicEnabled(enabled)
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch {
            repository?.toggleHaptics(enabled)
        }
    }

    fun startMusic() {
        if (sessionState.value.musicEnabled) {
            audioPlayer?.startMusic()
        }
    }

    fun stopMusic() {
        audioPlayer?.stopMusic()
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

    override fun onCleared() {
        super.onCleared()
        audioPlayer?.release()
    }
}
