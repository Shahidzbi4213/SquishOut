package com.squishout.game.util

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS: gesture/back navigation is handled by iOS navigation controllers
}
