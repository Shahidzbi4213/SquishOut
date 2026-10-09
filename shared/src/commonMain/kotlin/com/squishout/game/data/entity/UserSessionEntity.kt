package com.squishout.game.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 1,
    val diamonds: Int = 50,
    val lives: Int = 5,
    val maxLives: Int = 5,
    val lastLifeRefillEpoch: Long = 0L,
    val currentStage: Int = 1,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val loginStreakDays: Int = 1,
    val lastClaimEpochDay: Long = 0L,
    val claimedStarChests: String = "",
    val dailyPuzzleStreak: Int = 0,
    val lastDailyPuzzleEpochDay: Long = 0L,
    val claimedMonthlyMilestones: String = "",
    val hasCompletedTutorial: Boolean = false
)
