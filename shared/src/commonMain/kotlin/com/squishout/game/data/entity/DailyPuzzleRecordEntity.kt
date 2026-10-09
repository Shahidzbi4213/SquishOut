package com.squishout.game.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_puzzle_records")
data class DailyPuzzleRecordEntity(
    @PrimaryKey
    val epochDay: Long,
    val dateString: String,
    val dayOfMonth: Int,
    val monthKey: String,
    val stars: Int,
    val movesUsed: Int,
    val score: Int,
    val completedAt: Long
)
