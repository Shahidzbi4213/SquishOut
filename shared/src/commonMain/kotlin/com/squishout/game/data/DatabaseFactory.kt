package com.squishout.game.data

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

interface DatabaseFactory {
    fun createDatabase(): SquishDatabase
}

fun configureDatabaseBuilder(builder: RoomDatabase.Builder<SquishDatabase>): SquishDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
