package com.squishout.game.board

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import com.squishout.engine.model.Direction
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Allocation-free particle system designed for 60/120fps rendering on DrawScope.
 * Uses flat primitive arrays to prevent GC pauses during intense gameplay.
 */
class ParticleSystem(val maxParticles: Int = 180) {

    private val x = FloatArray(maxParticles)
    private val y = FloatArray(maxParticles)
    private val vx = FloatArray(maxParticles)
    private val vy = FloatArray(maxParticles)
    private val life = FloatArray(maxParticles) // Remaining life in seconds
    private val maxLife = FloatArray(maxParticles)
    private val size = FloatArray(maxParticles)
    private val gravity = FloatArray(maxParticles)
    private val colors = IntArray(maxParticles)
    private val shape = IntArray(maxParticles) // 0 = Circle droplet, 1 = Star sparkle, 2 = Shard

    var activeCount: Int = 0
        private set

    // Reusable scratch path for star/shard rendering to prevent per-particle Path allocation
    private val starPath = Path()

    /**
     * Spawns gelatin juice droplets that spray backward like rocket exhaust opposite to launch direction.
     */
    fun spawnJuiceDroplets(
        originX: Float,
        originY: Float,
        color: Color,
        direction: Direction,
        count: Int = 8
    ) {
        val argb = color.toArgb()
        // Base ejection angle is opposite to movement direction
        val baseAngle = when (direction) {
            Direction.EAST -> PI.toFloat() // sprays WEST
            Direction.WEST -> 0f // sprays EAST
            Direction.SOUTH -> -PI.toFloat() / 2f // sprays NORTH
            Direction.NORTH -> PI.toFloat() / 2f // sprays SOUTH
        }

        for (i in 0 until count) {
            if (activeCount >= maxParticles) break
            val idx = activeCount++

            // Jitter around tail of jelly
            x[idx] = originX + (Random.nextFloat() - 0.5f) * 16f
            y[idx] = originY + (Random.nextFloat() - 0.5f) * 16f

            // Cone spread of ~60 degrees
            val spread = (Random.nextFloat() - 0.5f) * (PI.toFloat() / 3f)
            val angle = baseAngle + spread
            val speed = Random.nextFloat() * 260f + 90f

            vx[idx] = cos(angle) * speed
            vy[idx] = sin(angle) * speed

            val duration = Random.nextFloat() * 0.22f + 0.22f // 220ms - 440ms
            life[idx] = duration
            maxLife[idx] = duration
            size[idx] = Random.nextFloat() * 6f + 4f
            gravity[idx] = 320f // Gentle downward pull
            colors[idx] = argb
            shape[idx] = 0 // Circle droplet
        }
    }

    /**
     * Spawns shards of cracked obstacles or coral.
     */
    fun spawnObstacleShatter(
        originX: Float,
        originY: Float,
        color: Color,
        count: Int = 14
    ) {
        val argb = color.toArgb()
        for (i in 0 until count) {
            if (activeCount >= maxParticles) break
            val idx = activeCount++

            x[idx] = originX + (Random.nextFloat() - 0.5f) * 20f
            y[idx] = originY + (Random.nextFloat() - 0.5f) * 20f

            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 320f + 110f

            vx[idx] = cos(angle) * speed
            vy[idx] = sin(angle) * speed

            val duration = Random.nextFloat() * 0.28f + 0.25f
            life[idx] = duration
            maxLife[idx] = duration
            size[idx] = Random.nextFloat() * 8f + 5f
            gravity[idx] = 600f // Heavier rock/coral gravity
            colors[idx] = argb
            shape[idx] = 2 // Shard
        }
    }

    /**
     * Spawns water/gelatin splash when a jelly breaches the tray rim.
     */
    fun spawnRimSplash(
        originX: Float,
        originY: Float,
        color: Color,
        exitDir: Direction,
        count: Int = 10
    ) {
        val argb = color.toArgb()
        val baseAngle = when (exitDir) {
            Direction.EAST -> 0f
            Direction.WEST -> PI.toFloat()
            Direction.SOUTH -> PI.toFloat() / 2f
            Direction.NORTH -> -PI.toFloat() / 2f
        }

        for (i in 0 until count) {
            if (activeCount >= maxParticles) break
            val idx = activeCount++

            x[idx] = originX
            y[idx] = originY

            val spread = (Random.nextFloat() - 0.5f) * (PI.toFloat() * 0.7f)
            val angle = baseAngle + spread
            val speed = Random.nextFloat() * 280f + 120f

            vx[idx] = cos(angle) * speed
            vy[idx] = sin(angle) * speed

            val duration = Random.nextFloat() * 0.24f + 0.2f
            life[idx] = duration
            maxLife[idx] = duration
            size[idx] = Random.nextFloat() * 7f + 3f
            gravity[idx] = 450f
            colors[idx] = argb
            shape[idx] = 0 // Droplet
        }
    }

    /**
     * Spawns celebratory golden starburst particles for final jelly solve or booster activation.
     */
    fun spawnStarBurst(
        originX: Float,
        originY: Float,
        count: Int = 22
    ) {
        val goldColors = intArrayOf(
            0xFFFFD700.toInt(), // Gold
            0xFFFFA500.toInt(), // Orange Gold
            0xFFFFF8DC.toInt(), // Shimmer White
            0xFFFF69B4.toInt()  // Candy Pink
        )

        for (i in 0 until count) {
            if (activeCount >= maxParticles) break
            val idx = activeCount++

            x[idx] = originX
            y[idx] = originY

            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 340f + 130f

            vx[idx] = cos(angle) * speed
            vy[idx] = sin(angle) * speed

            val duration = Random.nextFloat() * 0.35f + 0.35f // 350ms - 700ms
            life[idx] = duration
            maxLife[idx] = duration
            size[idx] = Random.nextFloat() * 8f + 5f
            gravity[idx] = 200f
            colors[idx] = goldColors[Random.nextInt(goldColors.size)]
            shape[idx] = 1 // Diamond star sparkle
        }
    }

    /**
     * Updates physics and draws active particles.
     * Operates in-place with O(1) removal to guarantee 0 GC allocations.
     */
    fun updateAndDraw(drawScope: DrawScope, dt: Float) {
        val dtClamped = dt.coerceIn(0f, 0.05f)
        var i = 0

        while (i < activeCount) {
            life[i] -= dtClamped
            if (life[i] <= 0f) {
                // Delete particle in O(1) by swapping with tail
                activeCount--
                x[i] = x[activeCount]
                y[i] = y[activeCount]
                vx[i] = vx[activeCount]
                vy[i] = vy[activeCount]
                life[i] = life[activeCount]
                maxLife[i] = maxLife[activeCount]
                size[i] = size[activeCount]
                gravity[i] = gravity[activeCount]
                colors[i] = colors[activeCount]
                shape[i] = shape[activeCount]
                continue
            }

            // Update particle kinematics
            x[i] += vx[i] * dtClamped
            y[i] += vy[i] * dtClamped
            vy[i] += gravity[i] * dtClamped

            val normalizedLife = (life[i] / maxLife[i]).coerceIn(0f, 1f)
            val alpha = normalizedLife * normalizedLife // Quadratic fade out
            val currentRadius = size[i] * (0.35f + 0.65f * normalizedLife)
            val baseColor = Color(colors[i])
            val particleColor = baseColor.copy(alpha = alpha * baseColor.alpha)

            when (shape[i]) {
                0 -> {
                    // Juicy circle droplet with glistening center
                    drawScope.drawCircle(
                        color = particleColor,
                        radius = currentRadius,
                        center = Offset(x[i], y[i])
                    )
                    if (currentRadius > 3.5f) {
                        drawScope.drawCircle(
                            color = Color.White.copy(alpha = alpha * 0.7f),
                            radius = currentRadius * 0.35f,
                            center = Offset(x[i] - currentRadius * 0.25f, y[i] - currentRadius * 0.25f)
                        )
                    }
                }
                1 -> {
                    // 4-point diamond sparkle
                    val cx = x[i]
                    val cy = y[i]
                    val r = currentRadius * 1.2f
                    starPath.reset()
                    starPath.moveTo(cx, cy - r)
                    starPath.quadraticTo(cx, cy, cx + r, cy)
                    starPath.quadraticTo(cx, cy, cx, cy + r)
                    starPath.quadraticTo(cx, cy, cx - r, cy)
                    starPath.quadraticTo(cx, cy, cx, cy - r)
                    starPath.close()

                    drawScope.drawPath(
                        path = starPath,
                        color = particleColor
                    )
                }
                2 -> {
                    // Angular rocky / crystalline shard
                    drawScope.drawRect(
                        color = particleColor,
                        topLeft = Offset(x[i] - currentRadius * 0.6f, y[i] - currentRadius * 0.6f),
                        size = Size(currentRadius * 1.2f, currentRadius * 1.2f)
                    )
                }
            }
            i++
        }
    }

    fun clear() {
        activeCount = 0
    }
}
