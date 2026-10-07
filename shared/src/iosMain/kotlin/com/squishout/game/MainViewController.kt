package com.squishout.game

import androidx.compose.ui.window.ComposeUIViewController
import com.squishout.game.di.initKoin

private var koinInitialized = false

fun initKoinIos() {
    if (!koinInitialized) {
        initKoin()
        koinInitialized = true
    }
}

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoinIos()
    }
) {
    initKoinIos()
    App()
}