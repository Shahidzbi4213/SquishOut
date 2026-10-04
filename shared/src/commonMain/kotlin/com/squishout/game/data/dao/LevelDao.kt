package com.squishout.game.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.squishout.game.data.entity.LevelRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Query("SELECT * FROM level_records ORDER BY levelNumber ASC")
    fun getAllRecords(): Flow<List<LevelRecordEntity>>

    @Query("SELECT * FROM level_records WHERE levelNumber = :levelNumber")
    fun getRecord(levelNumber: Int): Flow<LevelRecordEntity?>

    @Query("SELECT MAX(levelNumber) FROM level_records WHERE stars > 0")
    suspend fun getMaxCompletedLevel(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: LevelRecordEntity)

    @Query("SELECT COUNT(*) FROM level_records WHERE stars > 0")
    fun getCompletedLevelCount(): Flow<Int>

    @Query("SELECT SUM(stars) FROM level_records")
    fun getTotalStars(): Flow<Int?>
}
