package com.squishout.game.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.squishout.game.data.entity.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_session WHERE id = 1")
    fun getSession(): Flow<UserSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(session: UserSessionEntity)

    @Query("UPDATE user_session SET candies = candies + :amount WHERE id = 1")
    suspend fun addCandies(amount: Int)

    @Query("UPDATE user_session SET gems = gems + :amount WHERE id = 1")
    suspend fun addGems(amount: Int)

    @Query("UPDATE user_session SET currentStage = :stage WHERE id = 1")
    suspend fun updateCurrentStage(stage: Int)

    @Query("UPDATE user_session SET soundEnabled = :enabled WHERE id = 1")
    suspend fun updateSoundEnabled(enabled: Boolean)

    @Query("UPDATE user_session SET musicEnabled = :enabled WHERE id = 1")
    suspend fun updateMusicEnabled(enabled: Boolean)

    @Query("UPDATE user_session SET hapticsEnabled = :enabled WHERE id = 1")
    suspend fun updateHapticsEnabled(enabled: Boolean)

    @Query("UPDATE user_session SET loginStreakDays = :streak, lastClaimEpochDay = :epochDay WHERE id = 1")
    suspend fun updateStreak(streak: Int, epochDay: Long)

    @Query("UPDATE user_session SET claimedStarChests = :claimed WHERE id = 1")
    suspend fun updateClaimedStarChests(claimed: String)

    @Query("UPDATE user_session SET dailyPuzzleStreak = :streak, lastDailyPuzzleEpochDay = :epochDay WHERE id = 1")
    suspend fun updateDailyPuzzleStreak(streak: Int, epochDay: Long)

    @Query("UPDATE user_session SET claimedMonthlyMilestones = :claimed WHERE id = 1")
    suspend fun updateClaimedMonthlyMilestones(claimed: String)
}
