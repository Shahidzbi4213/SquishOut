package com.squishout.game.board

import androidx.compose.ui.geometry.Offset
import com.squishout.engine.model.Direction
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.Position

data class LaunchAnimation(
    val jelly: Jelly,
    val exitPath: List<Position> = emptyList(),
    val progress: Float = 0f
) {
    val easedProgress: Float
        get() = 1f - (1f - progress) * (1f - progress)

    val scaleAlongDir: Float
        get() = when {
            // Anticipation squish (first 15% of launch)
            progress < 0.15f -> 0.8f + (progress / 0.15f) * 0.2f
            // Stretch during flight
            progress < 0.7f -> 1.35f
            // Settle as it leaves screen
            else -> 1.1f
        }

    val scalePerpendicular: Float
        get() = when {
            progress < 0.15f -> 1.25f
            progress < 0.7f -> 0.75f
            else -> 0.9f
        }

    val alpha: Float
        get() = (1f - (progress - 0.7f).coerceAtLeast(0f) / 0.3f).coerceIn(0f, 1f)
}

data class WobbleAnimation(
    val jellyId: String,
    val direction: Direction,
    val offset: Float // -1f to 1f sinusoidal
)
