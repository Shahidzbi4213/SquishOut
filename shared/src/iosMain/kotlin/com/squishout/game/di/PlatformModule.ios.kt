package com.squishout.game.di

import com.squishout.game.audio.AudioPlayer
import com.squishout.game.data.DatabaseFactory
import com.squishout.game.data.IosDatabaseFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DatabaseFactory> { IosDatabaseFactory() }
    single { AudioPlayer() }
}
