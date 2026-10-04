package com.squishout.game.di

import com.squishout.game.audio.AudioPlayer
import com.squishout.game.data.AndroidDatabaseFactory
import com.squishout.game.data.DatabaseFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DatabaseFactory> { AndroidDatabaseFactory(get()) }
    single { AudioPlayer(get()) }
}
