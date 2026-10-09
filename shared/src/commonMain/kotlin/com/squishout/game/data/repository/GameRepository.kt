package com.squishout.game.data.repository

import com.squishout.game.data.dao.JellySkinDao
import com.squishout.game.data.dao.LevelDao
import com.squishout.game.data.dao.UserSessionDao
import com.squishout.game.data.entity.JellySkinEntity
import com.squishout.game.data.entity.LevelRecordEntity
import com.squishout.game.data.entity.UserSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.min

class GameRepository(
    private val levelDao: LevelDao,
    private val skinDao: JellySkinDao,
    private val sessionDao: UserSessionDao,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    companion object {
        const val REFILL_INTERVAL_MS = 20 * 60 * 1000L // 20 minutes per life
        const val MAX_LIVES = 5
    }

    val session: StateFlow<UserSessionEntity> = sessionDao.getSession()
        .filterNotNull()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = UserSessionEntity()
        )

    val allLevels: Flow<List<LevelRecordEntity>> = levelDao.getAllRecords()
    val allSkins: Flow<List<JellySkinEntity>> = skinDao.getAllSkins()
    val equippedSkin: Flow<JellySkinEntity?> = skinDao.getEquippedSkin()
    val totalStars: Flow<Int?> = levelDao.getTotalStars()

    init {
        scope.launch {
            seedDefaultsIfEmpty()
            refillLivesIfNeeded()
        }
    }

    private suspend fun seedDefaultsIfEmpty() {
        val existingSession = sessionDao.getSession().firstOrNull()
        if (existingSession == null) {
            sessionDao.insertOrUpdate(
                UserSessionEntity(
                    id = 1,
                    candies = 150,
                    gems = 15,
                    lives = MAX_LIVES,
                    maxLives = MAX_LIVES,
                    lastLifeRefillEpoch = 0L,
                    currentStage = 1,
                    soundEnabled = true,
                    hapticsEnabled = true
                )
            )
        }

        val starterSkins = listOf(
            JellySkinEntity(
                jellyId = "strawberry_classic",
                name = "Strawberry Blob",
                rarity = "COMMON",
                isUnlocked = true,
                level = 1,
                isEquipped = true,
                perkDescription = "Plump & juicy starting companion"
            ),
            JellySkinEntity(
                jellyId = "blueberry_duo",
                name = "Blueberry Duo",
                rarity = "COMMON",
                isUnlocked = true,
                level = 1,
                isEquipped = false,
                perkDescription = "Two-step smooth slide rhythm"
            ),
            JellySkinEntity(
                jellyId = "lemon_spark",
                name = "Lemon Spark",
                rarity = "RARE",
                isUnlocked = false,
                level = 1,
                isEquipped = false,
                perkDescription = "+10% bonus Candies on 3-star finish"
            ),
            JellySkinEntity(
                jellyId = "kiwi_hopper",
                name = "Kiwi Hopper",
                rarity = "RARE",
                isUnlocked = false,
                level = 1,
                isEquipped = false,
                perkDescription = "+1 Free Hint per day"
            ),
            JellySkinEntity(
                jellyId = "grape_monarch",
                name = "Grape Monarch",
                rarity = "MYTHIC",
                isUnlocked = false,
                level = 1,
                isEquipped = false,
                perkDescription = "Crown shimmer & rainbow launch trails"
            )
        )
        skinDao.insertDefaults(starterSkins)
    }

    suspend fun refillLivesIfNeeded(currentEpochMs: Long = 0L) {
        val currentSession = sessionDao.getSession().firstOrNull() ?: return
        if (currentSession.lives >= currentSession.maxLives) return
        if (currentEpochMs == 0L || currentSession.lastLifeRefillEpoch == 0L) return

        val elapsed = currentEpochMs - currentSession.lastLifeRefillEpoch
        if (elapsed >= REFILL_INTERVAL_MS) {
            val recovered = (elapsed / REFILL_INTERVAL_MS).toInt()
            val newLives = min(currentSession.maxLives, currentSession.lives + recovered)
            val newEpoch = if (newLives >= currentSession.maxLives) {
                0L
            } else {
                currentSession.lastLifeRefillEpoch + (recovered * REFILL_INTERVAL_MS)
            }
            sessionDao.insertOrUpdate(
                currentSession.copy(
                    lives = newLives,
                    lastLifeRefillEpoch = newEpoch
                )
            )
        }
    }

    suspend fun consumeLife(currentEpochMs: Long): Boolean {
        val currentSession = sessionDao.getSession().firstOrNull() ?: return false
        if (currentSession.lives <= 0) return false

        val newLives = currentSession.lives - 1
        val newEpoch = if (currentSession.lives == currentSession.maxLives) {
            currentEpochMs
        } else {
            currentSession.lastLifeRefillEpoch
        }

        sessionDao.insertOrUpdate(
            currentSession.copy(
                lives = newLives,
                lastLifeRefillEpoch = newEpoch
            )
        )
        return true
    }

    suspend fun recordLevelCompletion(
        levelNumber: Int,
        stars: Int,
        score: Int,
        movesUsed: Int,
        timestampEpoch: Long,
        difficultyTier: com.squishout.engine.model.DifficultyTier = com.squishout.engine.model.DifficultyTier.NORMAL
    ) {
        val existing = levelDao.getRecord(levelNumber).firstOrNull()
        val bestStars = if (existing != null) maxOf(existing.stars, stars) else stars
        val bestScore = if (existing != null) maxOf(existing.highScore, score) else score
        val bestMoves = if (existing != null) minOf(existing.movesUsed, movesUsed) else movesUsed

        levelDao.insertOrUpdate(
            LevelRecordEntity(
                levelNumber = levelNumber,
                stars = bestStars,
                highScore = bestScore,
                movesUsed = bestMoves,
                completedAt = timestampEpoch
            )
        )

        // Award candies: 25 base + 15 per star, boosted by difficulty tier
        val baseCandies = 25 + (stars * 15)
        val candiesAwarded = when (difficultyTier) {
            com.squishout.engine.model.DifficultyTier.SUPER_HARD -> baseCandies * 2
            com.squishout.engine.model.DifficultyTier.HARD -> (baseCandies * 1.5).toInt()
            else -> baseCandies
        }
        sessionDao.addCandies(candiesAwarded)

        // Super Hard boss victory bonus: 5 gems!
        if (difficultyTier == com.squishout.engine.model.DifficultyTier.SUPER_HARD) {
            sessionDao.addGems(5)
        }

        // Advance unlocked stage if current level completed
        val currentSession = sessionDao.getSession().firstOrNull()
        if (currentSession != null && levelNumber >= currentSession.currentStage) {
            sessionDao.updateCurrentStage(levelNumber + 1)
        }
    }

    suspend fun spendCandies(amount: Int): Boolean {
        val currentSession = sessionDao.getSession().firstOrNull() ?: return false
        if (currentSession.candies < amount) return false
        sessionDao.addCandies(-amount)
        return true
    }

    suspend fun spendGems(amount: Int): Boolean {
        val currentSession = sessionDao.getSession().firstOrNull() ?: return false
        if (currentSession.gems < amount) return false
        sessionDao.addGems(-amount)
        return true
    }

    suspend fun reviveWithGems(costGems: Int = 10): Boolean {
        if (!spendGems(costGems)) return false
        val currentSession = sessionDao.getSession().firstOrNull() ?: return false
        sessionDao.insertOrUpdate(
            currentSession.copy(
                lives = currentSession.maxLives,
                lastLifeRefillEpoch = 0L
            )
        )
        return true
    }

    suspend fun reviveWithAd(): Boolean {
        val currentSession = sessionDao.getSession().firstOrNull() ?: return false
        val newLives = min(currentSession.maxLives, currentSession.lives + 1)
        sessionDao.insertOrUpdate(
            currentSession.copy(
                lives = newLives,
                lastLifeRefillEpoch = if (newLives >= currentSession.maxLives) 0L else currentSession.lastLifeRefillEpoch
            )
        )
        return true
    }

    suspend fun equipSkin(jellyId: String) {
        skinDao.equipSkin(jellyId)
    }

    suspend fun unlockSkin(jellyId: String, costCandies: Int): Boolean {
        if (!spendCandies(costCandies)) return false
        skinDao.insertOrUpdate(
            JellySkinEntity(
                jellyId = jellyId,
                name = jellyId.replace("_", " ").replaceFirstChar { it.uppercase() },
                rarity = "RARE",
                isUnlocked = true,
                level = 1,
                isEquipped = false
            )
        )
        return true
    }

    suspend fun toggleSound(enabled: Boolean) {
        sessionDao.updateSoundEnabled(enabled)
    }

    suspend fun toggleMusic(enabled: Boolean) {
        sessionDao.updateMusicEnabled(enabled)
    }

    suspend fun toggleHaptics(enabled: Boolean) {
        sessionDao.updateHapticsEnabled(enabled)
    }

    suspend fun claimDailyReward(currentEpochMs: Long): DailyReward? {
        val currentSession = sessionDao.getSession().firstOrNull() ?: return null
        val currentDay = currentEpochMs / (24 * 60 * 60 * 1000L)
        if (currentDay == currentSession.lastClaimEpochDay) {
            return null // Already claimed today
        }

        val newStreak = when {
            currentSession.lastClaimEpochDay == 0L -> 1
            currentDay == currentSession.lastClaimEpochDay + 1L -> {
                val next = currentSession.loginStreakDays + 1
                if (next > 7) 1 else next
            }
            else -> 1 // Streak broken, restart at day 1
        }

        val reward = DAILY_REWARDS_SCHEDULE[(newStreak - 1).coerceIn(0, 6)]
        if (reward.candies > 0) sessionDao.addCandies(reward.candies)
        if (reward.gems > 0) sessionDao.addGems(reward.gems)
        if (reward.lives > 0) {
            val newLives = min(currentSession.maxLives, currentSession.lives + reward.lives)
            sessionDao.insertOrUpdate(currentSession.copy(lives = newLives))
        }
        sessionDao.updateStreak(newStreak, currentDay)
        return reward
    }

    fun canClaimDailyReward(currentEpochMs: Long): Boolean {
        val currentSession = session.value
        val currentDay = currentEpochMs / (24 * 60 * 60 * 1000L)
        return currentDay != currentSession.lastClaimEpochDay
    }
}

data class DailyReward(
    val day: Int,
    val rewardTitle: String,
    val icon: String,
    val candies: Int = 0,
    val gems: Int = 0,
    val lives: Int = 0
)

val DAILY_REWARDS_SCHEDULE = listOf(
    DailyReward(day = 1, rewardTitle = "50 Candies", icon = "🍬", candies = 50),
    DailyReward(day = 2, rewardTitle = "+1 Life", icon = "❤️", lives = 1),
    DailyReward(day = 3, rewardTitle = "75 Candies", icon = "🍬", candies = 75),
    DailyReward(day = 4, rewardTitle = "10 Gems", icon = "💎", gems = 10),
    DailyReward(day = 5, rewardTitle = "100 Candies", icon = "🍬", candies = 100),
    DailyReward(day = 6, rewardTitle = "15 Gems", icon = "💎", gems = 15),
    DailyReward(day = 7, rewardTitle = "Mega Box", icon = "🎁", candies = 250, gems = 25)
)
