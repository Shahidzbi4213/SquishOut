package com.squishout.game.data.repository

import com.squishout.game.data.dao.DailyPuzzleDao
import com.squishout.game.data.dao.JellySkinDao
import com.squishout.game.data.dao.LevelDao
import com.squishout.game.data.dao.UserSessionDao
import com.squishout.game.data.entity.DailyPuzzleRecordEntity
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.min

class GameRepository(
    private val levelDao: LevelDao,
    private val skinDao: JellySkinDao,
    private val sessionDao: UserSessionDao,
    private val dailyPuzzleDao: DailyPuzzleDao,
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
    val hasCompletedTutorial: Flow<Boolean> = session.map { it.hasCompletedTutorial }

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
                    diamonds = 50,
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
                perkDescription = "+10% bonus Diamonds on 3-star finish"
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
            ),
            JellySkinEntity(
                jellyId = "cosmic_nebula",
                name = "Cosmic Nebula",
                rarity = "LEGENDARY",
                isUnlocked = false,
                level = 1,
                isEquipped = false,
                perkDescription = "Daily puzzle mastery: Stardust aura & cosmic glow"
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

        // Award diamonds: 10 base + 5 per star, boosted by difficulty tier
        val baseDiamonds = 10 + (stars * 5)
        val diamondsAwarded = when (difficultyTier) {
            com.squishout.engine.model.DifficultyTier.SUPER_HARD -> baseDiamonds * 2
            com.squishout.engine.model.DifficultyTier.HARD -> (baseDiamonds * 1.5).toInt()
            else -> baseDiamonds
        }
        sessionDao.addDiamonds(diamondsAwarded)

        // Advance unlocked stage if current level completed
        val currentSession = sessionDao.getSession().firstOrNull()
        if (currentSession != null && levelNumber >= currentSession.currentStage) {
            sessionDao.updateCurrentStage(levelNumber + 1)
        }
        if (levelNumber == 1) {
            sessionDao.updateHasCompletedTutorial(true)
        }
    }

    suspend fun spendDiamonds(amount: Int): Boolean {
        val currentSession = sessionDao.getSession().firstOrNull() ?: return false
        if (currentSession.diamonds < amount) return false
        sessionDao.addDiamonds(-amount)
        return true
    }

    suspend fun addDiamonds(amount: Int) {
        sessionDao.addDiamonds(amount)
    }

    suspend fun reviveWithDiamonds(costDiamonds: Int = 10): Boolean {
        if (!spendDiamonds(costDiamonds)) return false
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

    suspend fun unlockSkin(jellyId: String, costDiamonds: Int): Boolean {
        if (!spendDiamonds(costDiamonds)) return false
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

    suspend fun completeTutorial() {
        sessionDao.updateHasCompletedTutorial(true)
    }

    suspend fun skipTutorial() {
        completeTutorial()
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
        if (reward.diamonds > 0) sessionDao.addDiamonds(reward.diamonds)
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

    suspend fun claimStarChest(milestone: StarChestMilestone, totalStars: Int): Boolean {
        if (totalStars < milestone.requiredStars) return false
        val currentSession = sessionDao.getSession().firstOrNull() ?: return false
        val claimedList = currentSession.claimedStarChests.split(",").filter { it.isNotBlank() }.toMutableSet()
        val key = milestone.requiredStars.toString()
        if (claimedList.contains(key)) return false // Already claimed

        claimedList.add(key)
        sessionDao.insertOrUpdate(
            currentSession.copy(
                diamonds = currentSession.diamonds + milestone.diamonds,
                claimedStarChests = claimedList.joinToString(",")
            )
        )
        return true
    }

    fun getDailyRecordsForMonth(monthKey: String): Flow<List<DailyPuzzleRecordEntity>> =
        dailyPuzzleDao.getRecordsForMonth(monthKey)

    fun getDailyRecordForDay(epochDay: Long): Flow<DailyPuzzleRecordEntity?> =
        dailyPuzzleDao.getRecordForDay(epochDay)

    suspend fun recordDailyPuzzleCompletion(
        epochDay: Long,
        dateString: String,
        dayOfMonth: Int,
        monthKey: String,
        stars: Int,
        movesUsed: Int,
        score: Int,
        currentEpochMs: Long = 0L
    ): Boolean {
        val now = if (currentEpochMs > 0L) currentEpochMs else com.squishout.game.util.currentTimeMillis()
        val record = DailyPuzzleRecordEntity(
            epochDay = epochDay,
            dateString = dateString,
            dayOfMonth = dayOfMonth,
            monthKey = monthKey,
            stars = stars,
            movesUsed = movesUsed,
            score = score,
            completedAt = now
        )
        dailyPuzzleDao.insertOrUpdate(record)

        val currentSession = sessionDao.getSession().firstOrNull() ?: return true
        val lastDay = currentSession.lastDailyPuzzleEpochDay
        val newStreak = when {
            lastDay == epochDay -> currentSession.dailyPuzzleStreak // already completed today
            lastDay == epochDay - 1 -> currentSession.dailyPuzzleStreak + 1 // consecutive day
            else -> 1 // reset streak to 1
        }

        // Daily puzzle reward: +25 diamonds
        val updatedSession = currentSession.copy(
            diamonds = currentSession.diamonds + 25,
            dailyPuzzleStreak = newStreak,
            lastDailyPuzzleEpochDay = epochDay
        )
        sessionDao.insertOrUpdate(updatedSession)
        return true
    }

    suspend fun claimMonthlyMilestone(
        monthKey: String,
        milestoneDays: Int,
        monthlyCompletionCount: Int
    ): Boolean {
        if (monthlyCompletionCount < milestoneDays) return false
        val currentSession = sessionDao.getSession().firstOrNull() ?: return false
        val claimedSet = currentSession.claimedMonthlyMilestones.split(",").filter { it.isNotBlank() }.toMutableSet()
        val key = "$monthKey:$milestoneDays"
        if (claimedSet.contains(key)) return false

        claimedSet.add(key)
        var addedDiamonds = 0

        when (milestoneDays) {
            5 -> addedDiamonds = 50
            10 -> addedDiamonds = 100
            20 -> {
                addedDiamonds = 150
                // Unlock Cosmic Nebula skin
                skinDao.insertOrUpdate(
                    JellySkinEntity(
                        jellyId = "cosmic_nebula",
                        name = "Cosmic Nebula",
                        rarity = "LEGENDARY",
                        isUnlocked = true,
                        level = 1,
                        isEquipped = false,
                        perkDescription = "Daily puzzle mastery: Stardust aura & cosmic glow"
                    )
                )
            }
        }

        sessionDao.insertOrUpdate(
            currentSession.copy(
                diamonds = currentSession.diamonds + addedDiamonds,
                claimedMonthlyMilestones = claimedSet.joinToString(",")
            )
        )
        return true
    }
}

data class StarChestMilestone(
    val requiredStars: Int,
    val stageAnchor: Int,
    val diamonds: Int,
    val hintBoosters: Int = 0,
    val undoBoosters: Int = 0,
    val wandBoosters: Int = 0
)

val STAR_CHEST_MILESTONES = listOf(
    StarChestMilestone(requiredStars = 15, stageAnchor = 10, diamonds = 25, hintBoosters = 1),
    StarChestMilestone(requiredStars = 30, stageAnchor = 20, diamonds = 40, wandBoosters = 1),
    StarChestMilestone(requiredStars = 50, stageAnchor = 35, diamonds = 60, undoBoosters = 2),
    StarChestMilestone(requiredStars = 75, stageAnchor = 50, diamonds = 80, hintBoosters = 2),
    StarChestMilestone(requiredStars = 100, stageAnchor = 70, diamonds = 120, wandBoosters = 2),
    StarChestMilestone(requiredStars = 150, stageAnchor = 100, diamonds = 200, hintBoosters = 2, undoBoosters = 2, wandBoosters = 2)
)

data class DailyReward(
    val day: Int,
    val rewardTitle: String,
    val icon: String,
    val diamonds: Int = 0,
    val lives: Int = 0
)

val DAILY_REWARDS_SCHEDULE = listOf(
    DailyReward(day = 1, rewardTitle = "15 Diamonds", icon = "💎", diamonds = 15),
    DailyReward(day = 2, rewardTitle = "+1 Life", icon = "❤️", lives = 1),
    DailyReward(day = 3, rewardTitle = "25 Diamonds", icon = "💎", diamonds = 25),
    DailyReward(day = 4, rewardTitle = "35 Diamonds", icon = "💎", diamonds = 35),
    DailyReward(day = 5, rewardTitle = "50 Diamonds", icon = "💎", diamonds = 50),
    DailyReward(day = 6, rewardTitle = "75 Diamonds", icon = "💎", diamonds = 75),
    DailyReward(day = 7, rewardTitle = "Mega Box", icon = "🎁", diamonds = 150)
)

data class MonthlyMilestone(
    val requiredDays: Int,
    val rewardTitle: String,
    val iconEmoji: String,
    val diamonds: Int = 0,
    val skinRewardId: String? = null
)

val MONTHLY_MILESTONES = listOf(
    MonthlyMilestone(requiredDays = 5, rewardTitle = "50 Diamonds", iconEmoji = "💎", diamonds = 50),
    MonthlyMilestone(requiredDays = 10, rewardTitle = "100 Diamonds", iconEmoji = "💎", diamonds = 100),
    MonthlyMilestone(requiredDays = 20, rewardTitle = "Cosmic Nebula Skin", iconEmoji = "🌌", diamonds = 150, skinRewardId = "cosmic_nebula")
)

