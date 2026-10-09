package com.squishout.game

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.squishout.game.audio.AudioPlayer
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val audioPlayer: AudioPlayer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Extend into display cutout area (camera notch/hole) for edge-to-edge immersion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        hideSystemStatusBar()

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        audioPlayer.resumeAll()
    }

    override fun onPause() {
        super.onPause()
        audioPlayer.pauseAll()
    }

    override fun onStop() {
        super.onStop()
        audioPlayer.pauseAll()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemStatusBar()
        }
    }

    private fun hideSystemStatusBar() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}