package com.squishout.game.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.squishout.game.data.entity.DailyPuzzleRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPuzzleDao {
    @Query("SELECT * FROM daily_puzzle_records WHERE monthKey = :monthKey ORDER BY dayOfMonth ASC")
    fun getRecordsForMonth(monthKey: String): Flow<List<DailyPuzzleRecordEntity>>

    @Query("SELECT * FROM daily_puzzle_records WHERE epochDay = :epochDay")
    fun getRecordForDay(epochDay: Long): Flow<DailyPuzzleRecordEntity?>

    @Query("SELECT * FROM daily_puzzle_records ORDER BY epochDay DESC")
    fun getAllRecords(): Flow<List<DailyPuzzleRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: DailyPuzzleRecordEntity)
}
