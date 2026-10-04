package com.squishout.game.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_records")
data class LevelRecordEntity(
    @PrimaryKey val levelNumber: Int,
    val stars: Int,
    val highScore: Int,
    val movesUsed: Int,
    val completedAt: Long
)
