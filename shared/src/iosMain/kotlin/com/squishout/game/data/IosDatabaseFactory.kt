package com.squishout.game.data

import androidx.room.Room
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IosDatabaseFactory : DatabaseFactory {
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    override fun createDatabase(): SquishDatabase {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        val path = requireNotNull(documentDirectory?.path) + "/squish_out.db"
        val builder = Room.databaseBuilder<SquishDatabase>(
            name = path
        )
        return configureDatabaseBuilder(builder)
    }
}
