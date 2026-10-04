package com.squishout.game.di

import com.squishout.engine.GameEngine
import com.squishout.engine.generator.ReverseAssemblyGenerator
import com.squishout.game.data.DatabaseFactory
import com.squishout.game.data.SquishDatabase
import com.squishout.game.data.dao.JellySkinDao
import com.squishout.game.data.dao.LevelDao
import com.squishout.game.data.dao.UserSessionDao
import com.squishout.game.data.repository.GameRepository
import com.squishout.game.presentation.GameViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val databaseModule = module {
    single<SquishDatabase> { get<DatabaseFactory>().createDatabase() }
    single<LevelDao> { get<SquishDatabase>().levelDao() }
    single<JellySkinDao> { get<SquishDatabase>().jellySkinDao() }
    single<UserSessionDao> { get<SquishDatabase>().userSessionDao() }
    single { GameRepository(get(), get(), get()) }
}

val engineModule = module {
    factory { GameEngine() }
    factory { ReverseAssemblyGenerator() }
}

val viewModelModule = module {
    viewModelOf(::GameViewModel)
}

val appModule = module {
    includes(platformModule, databaseModule, engineModule, viewModelModule)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule)
    }

fun initKoin() = initKoin {}
