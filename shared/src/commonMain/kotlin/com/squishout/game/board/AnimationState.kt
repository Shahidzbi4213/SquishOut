package com.squishout.game.board

import androidx.compose.ui.geometry.Offset
import com.squishout.engine.model.Direction
import com.squishout.engine.model.Jelly

data class LaunchAnimation(
    val jelly: Jelly,
    val startOffset: Offset,
    val exitDirection: Direction,
    val distance: Float,
    val progress: Float = 0f
) {
    val currentOffset: Offset
        get() {
            val easeOutQuad = 1f - (1f - progress) * (1f - progress)
            val traveled = distance * easeOutQuad
            return Offset(
                startOffset.x + exitDirection.dx * traveled,
                startOffset.y + exitDirection.dy * traveled
            )
        }

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
