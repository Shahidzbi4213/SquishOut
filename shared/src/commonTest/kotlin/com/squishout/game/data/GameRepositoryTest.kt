package com.squishout.game.data

import com.squishout.game.data.dao.DailyPuzzleDao
import com.squishout.game.data.dao.JellySkinDao
import com.squishout.game.data.dao.LevelDao
import com.squishout.game.data.dao.UserSessionDao
import com.squishout.game.data.entity.DailyPuzzleRecordEntity
import com.squishout.game.data.entity.JellySkinEntity
import com.squishout.game.data.entity.LevelRecordEntity
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.data.repository.GameRepository
import com.squishout.game.data.repository.STAR_CHEST_MILESTONES
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeLevelDao : LevelDao {
    private val records = MutableStateFlow<Map<Int, LevelRecordEntity>>(emptyMap())

    override fun getAllRecords(): Flow<List<LevelRecordEntity>> =
        records.map { it.values.sortedBy { r -> r.levelNumber } }

    override fun getRecord(levelNumber: Int): Flow<LevelRecordEntity?> =
        records.map { it[levelNumber] }

    override suspend fun getMaxCompletedLevel(): Int? =
        records.value.values.filter { it.stars > 0 }.maxOfOrNull { it.levelNumber }

    override suspend fun insertOrUpdate(record: LevelRecordEntity) {
        records.value = records.value + (record.levelNumber to record)
    }

    override fun getCompletedLevelCount(): Flow<Int> =
        records.map { it.values.count { r -> r.stars > 0 } }

    override fun getTotalStars(): Flow<Int?> =
        records.map { it.values.sumOf { r -> r.stars } }
}

class FakeJellySkinDao : JellySkinDao {
    private val skins = MutableStateFlow<Map<String, JellySkinEntity>>(emptyMap())

    override fun getAllSkins(): Flow<List<JellySkinEntity>> =
        skins.map { it.values.toList() }

    override fun getEquippedSkin(): Flow<JellySkinEntity?> =
        skins.map { it.values.firstOrNull { s -> s.isEquipped } }

    override suspend fun insertDefaults(newSkins: List<JellySkinEntity>) {
        val current = skins.value.toMutableMap()
        for (skin in newSkins) {
            if (!current.containsKey(skin.jellyId)) {
                current[skin.jellyId] = skin
            }
        }
        skins.value = current
    }

    override suspend fun insertOrUpdate(skin: JellySkinEntity) {
        skins.value = skins.value + (skin.jellyId to skin)
    }

    override suspend fun clearEquipped() {
        skins.value = skins.value.mapValues { it.value.copy(isEquipped = false) }
    }

    override suspend fun markEquipped(jellyId: String) {
        skins.value = skins.value.mapValues {
            if (it.key == jellyId) it.value.copy(isEquipped = true) else it.value
        }
    }
}

class FakeUserSessionDao : UserSessionDao {
    private val session = MutableStateFlow<UserSessionEntity?>(null)

    override fun getSession(): Flow<UserSessionEntity?> = session

    override suspend fun insertOrUpdate(newSession: UserSessionEntity) {
        session.value = newSession
    }

    override suspend fun addCandies(amount: Int) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(candies = cur.candies + amount)
    }

    override suspend fun addGems(amount: Int) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(gems = cur.gems + amount)
    }

    override suspend fun updateCurrentStage(stage: Int) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(currentStage = stage)
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(soundEnabled = enabled)
    }

    override suspend fun updateMusicEnabled(enabled: Boolean) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(musicEnabled = enabled)
    }

    override suspend fun updateHapticsEnabled(enabled: Boolean) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(hapticsEnabled = enabled)
    }

    override suspend fun updateStreak(streak: Int, epochDay: Long) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(loginStreakDays = streak, lastClaimEpochDay = epochDay)
    }

    override suspend fun updateClaimedStarChests(claimed: String) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(claimedStarChests = claimed)
    }

    override suspend fun updateDailyPuzzleStreak(streak: Int, epochDay: Long) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(dailyPuzzleStreak = streak, lastDailyPuzzleEpochDay = epochDay)
    }

    override suspend fun updateClaimedMonthlyMilestones(claimed: String) {
        val cur = session.value ?: UserSessionEntity()
        session.value = cur.copy(claimedMonthlyMilestones = claimed)
    }
}

class FakeDailyPuzzleDao : DailyPuzzleDao {
    private val records = MutableStateFlow<Map<Long, DailyPuzzleRecordEntity>>(emptyMap())

    override fun getRecordsForMonth(monthKey: String): Flow<List<DailyPuzzleRecordEntity>> =
        records.map { it.values.filter { r -> r.monthKey == monthKey }.sortedBy { r -> r.dayOfMonth } }

    override fun getRecordForDay(epochDay: Long): Flow<DailyPuzzleRecordEntity?> =
        records.map { it[epochDay] }

    override fun getAllRecords(): Flow<List<DailyPuzzleRecordEntity>> =
        records.map { it.values.sortedByDescending { r -> r.epochDay } }

    override suspend fun insertOrUpdate(record: DailyPuzzleRecordEntity) {
        records.value = records.value + (record.epochDay to record)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GameRepositoryTest {

    private lateinit var levelDao: FakeLevelDao
    private lateinit var skinDao: FakeJellySkinDao
    private lateinit var sessionDao: FakeUserSessionDao
    private lateinit var dailyPuzzleDao: FakeDailyPuzzleDao
    private lateinit var repository: GameRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @BeforeTest
    fun setup() {
        levelDao = FakeLevelDao()
        skinDao = FakeJellySkinDao()
        sessionDao = FakeUserSessionDao()
        dailyPuzzleDao = FakeDailyPuzzleDao()
        repository = GameRepository(levelDao, skinDao, sessionDao, dailyPuzzleDao, testScope)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun testInitialSeeding() = runTest(testDispatcher) {
        val session = repository.session.value
        assertEquals(150, session.candies)
        assertEquals(15, session.gems)
        assertEquals(5, session.lives)
        assertEquals(1, session.currentStage)
        assertTrue(session.soundEnabled)
        assertTrue(session.hapticsEnabled)
    }

    @Test
    fun testConsumeLife() = runTest(testDispatcher) {
        val startTime = 1000000L
        val consumed = repository.consumeLife(startTime)
        testScheduler.advanceUntilIdle()
        assertTrue(consumed)
        assertEquals(4, repository.session.value.lives)
        assertEquals(startTime, repository.session.value.lastLifeRefillEpoch)

        // Consume all remaining lives
        repository.consumeLife(startTime + 1000)
        repository.consumeLife(startTime + 2000)
        repository.consumeLife(startTime + 3000)
        repository.consumeLife(startTime + 4000)
        testScheduler.advanceUntilIdle()
        assertEquals(0, repository.session.value.lives)

        // 6th consume must fail
        assertFalse(repository.consumeLife(startTime + 5000))
    }

    @Test
    fun testLifeRegenerationAfter20Minutes() = runTest(testDispatcher) {
        val startTime = 1000000L
        repository.consumeLife(startTime) // 4 lives
        testScheduler.advanceUntilIdle()
        assertEquals(4, repository.session.value.lives)

        // 10 minutes later: not enough time to regenerate
        val tenMinutesLater = startTime + (10 * 60 * 1000L)
        repository.refillLivesIfNeeded(tenMinutesLater)
        testScheduler.advanceUntilIdle()
        assertEquals(4, repository.session.value.lives)

        // 20 minutes later: exactly 1 life regenerated
        val twentyMinutesLater = startTime + (20 * 60 * 1000L)
        repository.refillLivesIfNeeded(twentyMinutesLater)
        testScheduler.advanceUntilIdle()
        assertEquals(5, repository.session.value.lives)
        assertEquals(0L, repository.session.value.lastLifeRefillEpoch)
    }

    @Test
    fun testLevelCompletionPersistenceAndProgression() = runTest(testDispatcher) {
        // Complete stage 1 with 3 stars and 1200 score
        repository.recordLevelCompletion(
            levelNumber = 1,
            stars = 3,
            score = 1200,
            movesUsed = 12,
            timestampEpoch = 123456789L
        )
        testScheduler.advanceUntilIdle()

        // Candies should increase by 25 base + 3*15 = 70 (150 + 70 = 220)
        assertEquals(220, repository.session.value.candies)
        // Stage progression advances to 2
        assertEquals(2, repository.session.value.currentStage)
    }

    @Test
    fun testEconomyAndRevives() = runTest(testDispatcher) {
        // Drain lives
        val now = 1000000L
        for (i in 0 until 5) {
            repository.consumeLife(now)
        }
        testScheduler.advanceUntilIdle()
        assertEquals(0, repository.session.value.lives)

        // Revive with ad gives 1 heart
        val adRevive = repository.reviveWithAd()
        testScheduler.advanceUntilIdle()
        assertTrue(adRevive)
        assertEquals(1, repository.session.value.lives)

        // Revive with gems (costs 10 gems)
        val gemRevive = repository.reviveWithGems(10)
        testScheduler.advanceUntilIdle()
        assertTrue(gemRevive)
        assertEquals(5, repository.session.value.lives)
        assertEquals(5, repository.session.value.gems) // 15 - 10 = 5

        // Another gem revive should fail because only 5 gems left
        val secondGemRevive = repository.reviveWithGems(10)
        testScheduler.advanceUntilIdle()
        assertFalse(secondGemRevive)
        assertEquals(5, repository.session.value.gems)
    }

    @Test
    fun testSkinEquippingAndUnlocking() = runTest(testDispatcher) {
        repository.equipSkin("blueberry_duo")
        testScheduler.advanceUntilIdle()

        // Unlock lemon spark for 100 candies
        val unlocked = repository.unlockSkin("lemon_spark", 100)
        testScheduler.advanceUntilIdle()
        assertTrue(unlocked)
        assertEquals(50, repository.session.value.candies) // 150 - 100 = 50

        // Attempting to spend 100 candies again should fail
        val failedUnlock = repository.unlockSkin("grape_monarch", 100)
        testScheduler.advanceUntilIdle()
        assertFalse(failedUnlock)
        assertEquals(50, repository.session.value.candies)
    }

    @Test
    fun testDailyRewardClaimAndStreakProgression() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val day1Ms = 1700000000000L
        val reward1 = repository.claimDailyReward(day1Ms)
        testScheduler.advanceUntilIdle()
        assertNotNull(reward1)
        assertEquals(1, reward1.day)
        assertEquals(50, reward1.candies)
        assertEquals(200, repository.session.value.candies) // 150 + 50 = 200

        // Same day claim must return null
        val duplicateClaim = repository.claimDailyReward(day1Ms + 1000L)
        testScheduler.advanceUntilIdle()
        assertNull(duplicateClaim)

        // Consecutive day claim -> Day 2 (+1 life)
        val day2Ms = day1Ms + 24 * 60 * 60 * 1000L
        val reward2 = repository.claimDailyReward(day2Ms)
        testScheduler.advanceUntilIdle()
        assertNotNull(reward2)
        assertEquals(2, reward2.day)
        assertEquals(2, repository.session.value.loginStreakDays)
    }

    @Test
    fun testToggleMusic() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        assertTrue(repository.session.value.musicEnabled)

        repository.toggleMusic(false)
        testScheduler.advanceUntilIdle()
        assertFalse(repository.session.value.musicEnabled)

        repository.toggleMusic(true)
        testScheduler.advanceUntilIdle()
        assertTrue(repository.session.value.musicEnabled)
    }

    @Test
    fun testClaimStarChest() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val milestone = STAR_CHEST_MILESTONES[0] // 15 stars, 100 candies, 10 gems

        // Fails if not enough stars
        val failed = repository.claimStarChest(milestone, totalStars = 10)
        assertFalse(failed)

        // Succeeds if total stars >= 15
        val initialCandies = repository.session.value.candies
        val initialGems = repository.session.value.gems
        val claimed = repository.claimStarChest(milestone, totalStars = 15)
        testScheduler.advanceUntilIdle()
        assertTrue(claimed)
        assertEquals(initialCandies + milestone.candies, repository.session.value.candies)
        assertEquals(initialGems + milestone.gems, repository.session.value.gems)
        assertTrue(repository.session.value.claimedStarChests.contains("15"))

        // Cannot claim again
        val duplicate = repository.claimStarChest(milestone, totalStars = 15)
        testScheduler.advanceUntilIdle()
        assertFalse(duplicate)
    }

    @Test
    fun testDailyPuzzleCompletionAndStreak() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val epochDay1 = 20000L
        val dateString1 = "2024-10-09"
        val dayOfMonth1 = 9
        val monthKey1 = "2024-10"

        val initialCandies = repository.session.value.candies
        val initialGems = repository.session.value.gems

        val success = repository.recordDailyPuzzleCompletion(
            epochDay = epochDay1,
            dateString = dateString1,
            dayOfMonth = dayOfMonth1,
            monthKey = monthKey1,
            stars = 3,
            movesUsed = 12,
            score = 1500,
            currentEpochMs = epochDay1 * 86400000L
        )
        testScheduler.advanceUntilIdle()
        assertTrue(success)
        assertEquals(initialCandies + 100, repository.session.value.candies)
        assertEquals(initialGems + 10, repository.session.value.gems)
        assertEquals(1, repository.session.value.dailyPuzzleStreak)
        assertEquals(epochDay1, repository.session.value.lastDailyPuzzleEpochDay)

        // Consecutive Day completion (Day + 1)
        val epochDay2 = epochDay1 + 1
        repository.recordDailyPuzzleCompletion(
            epochDay = epochDay2,
            dateString = "2024-10-10",
            dayOfMonth = 10,
            monthKey = monthKey1,
            stars = 3,
            movesUsed = 10,
            score = 1800,
            currentEpochMs = epochDay2 * 86400000L
        )
        testScheduler.advanceUntilIdle()
        assertEquals(2, repository.session.value.dailyPuzzleStreak)
    }

    @Test
    fun testMonthlyMilestoneClaim() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()
        val monthKey = "2024-10"

        // Fails if completion count < milestone
        val failedClaim = repository.claimMonthlyMilestone(monthKey, milestoneDays = 5, monthlyCompletionCount = 3)
        assertFalse(failedClaim)

        // Succeeds if completion count >= 5
        val initialCandies = repository.session.value.candies
        val successClaim = repository.claimMonthlyMilestone(monthKey, milestoneDays = 5, monthlyCompletionCount = 5)
        testScheduler.advanceUntilIdle()
        assertTrue(successClaim)
        assertEquals(initialCandies + 150, repository.session.value.candies)

        // Duplicate claim fails
        val duplicateClaim = repository.claimMonthlyMilestone(monthKey, milestoneDays = 5, monthlyCompletionCount = 5)
        testScheduler.advanceUntilIdle()
        assertFalse(duplicateClaim)

        // Claiming 20-day milestone unlocks cosmic_nebula skin
        val claim20 = repository.claimMonthlyMilestone(monthKey, milestoneDays = 20, monthlyCompletionCount = 20)
        testScheduler.advanceUntilIdle()
        assertTrue(claim20)
        val cosmicSkin = repository.allSkins.firstOrNull()?.find { it.jellyId == "cosmic_nebula" }
        assertNotNull(cosmicSkin)
        assertTrue(cosmicSkin.isUnlocked)
    }
}
