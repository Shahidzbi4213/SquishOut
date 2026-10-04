package com.squishout.game

import android.app.Application
import com.squishout.game.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class SquishOutApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@SquishOutApplication)
        }
    }
}
