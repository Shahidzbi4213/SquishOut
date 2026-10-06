package com.squishout.game.board

import androidx.compose.ui.geometry.Offset
import com.squishout.engine.model.Direction
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.Position
import kotlin.math.sin

data class LaunchAnimation(
    val jelly: Jelly,
    val exitPath: List<Position> = emptyList(),
    val progress: Float = 0f,
    val isFeverClimax: Boolean = false
) {
    val easedProgress: Float
        get() = if (isFeverClimax) {
            // Dramatic slow-motion ease with rapid final exit
            if (progress < 0.6f) {
                // Suspenseful slow drift
                (progress / 0.6f) * 0.45f
            } else {
                // Explosive supersonic exit
                val p = (progress - 0.6f) / 0.4f
                0.45f + (1f - (1f - p) * (1f - p)) * 0.55f
            }
        } else {
            1f - (1f - progress) * (1f - progress)
        }

    val scaleAlongDir: Float
        get() = when {
            // Anticipation squish (first 15% of launch)
            progress < 0.15f -> 0.78f + (progress / 0.15f) * 0.22f
            // High-speed aerodynamic stretch during flight
            progress < 0.7f -> if (isFeverClimax) 1.5f else 1.38f
            // Settle as it leaves screen
            else -> 1.1f
        }

    val scalePerpendicular: Float
        get() = when {
            progress < 0.15f -> 1.28f
            progress < 0.7f -> if (isFeverClimax) 0.68f else 0.74f
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

data class BlockerRecoil(
    val position: Position,
    val direction: Direction,
    val magnitude: Float // 0f to 1f damped recoil
)

data class FlyingRewardToken(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    val progress: Float,
    val isStar: Boolean = true,
    val arcSpread: Float = 0f
) {
    /**
     * Computes the current position along a dynamic quadratic Bezier flight path.
     */
    fun currentPosition(): Offset {
        val t = progress.coerceIn(0f, 1f)
        val oneMinusT = 1f - t

        // Apex control point in the sky above the mid-way point
        val midX = (startX + targetX) / 2f + arcSpread
        val apexY = minOf(startX, targetY) - 100f
        val ctrlY = minOf(startY, targetY) - 120f

        val x = oneMinusT * oneMinusT * startX + 2f * oneMinusT * t * midX + t * t * targetX
        val y = oneMinusT * oneMinusT * startY + 2f * oneMinusT * t * ctrlY + t * t * targetY

        return Offset(x, y)
    }

    val scale: Float
        get() = when {
            // Explosive initial burst
            progress < 0.2f -> 0.6f + (progress / 0.2f) * 0.7f // 0.6 -> 1.3
            // Steady flight
            progress < 0.8f -> 1.15f
            // Slight funneling shrink as it enters HUD pill
            else -> 1.15f - ((progress - 0.8f) / 0.2f) * 0.35f // 1.15 -> 0.8
        }
}
