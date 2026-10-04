package com.squishout.game.data

import android.content.Context
import androidx.room.Room

class AndroidDatabaseFactory(private val context: Context) : DatabaseFactory {
    override fun createDatabase(): SquishDatabase {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath("squish_out.db")
        val builder = Room.databaseBuilder<SquishDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
        return configureDatabaseBuilder(builder)
    }
}
