package com.squishout.game.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.squishout.game.data.dao.DailyPuzzleDao
import com.squishout.game.data.dao.JellySkinDao
import com.squishout.game.data.dao.LevelDao
import com.squishout.game.data.dao.UserSessionDao
import com.squishout.game.data.entity.DailyPuzzleRecordEntity
import com.squishout.game.data.entity.JellySkinEntity
import com.squishout.game.data.entity.LevelRecordEntity
import com.squishout.game.data.entity.UserSessionEntity

@Database(
    entities = [
        LevelRecordEntity::class,
        JellySkinEntity::class,
        UserSessionEntity::class,
        DailyPuzzleRecordEntity::class
    ],
    version = 7
)
@ConstructedBy(SquishDatabaseConstructor::class)
abstract class SquishDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
    abstract fun jellySkinDao(): JellySkinDao
    abstract fun userSessionDao(): UserSessionDao
    abstract fun dailyPuzzleDao(): DailyPuzzleDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object SquishDatabaseConstructor : RoomDatabaseConstructor<SquishDatabase> {
    override fun initialize(): SquishDatabase
}
