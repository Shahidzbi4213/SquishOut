package com.squishout.game.board

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.squishout.engine.model.Obstacle
import com.squishout.engine.model.ObstacleType
import com.squishout.game.theme.SquishColors

object TrayRenderer {

    // Pre-allocated scratch paths for zero-allocation obstacle rendering
    private val scratchMountainPath = Path()
    private val scratchCrackPath = Path()
    private val scratchArrowPath = Path()

    // Pre-allocated color lists and geometries for zero-allocation rendering
    private val waterJetColors = listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8), Color(0xFF0284C7))
    private val fogBubbleColors = listOf(
        Color(0xFFFFFFFF).copy(alpha = 0.92f),
        Color(0xFFE0F2FE).copy(alpha = 0.88f),
        Color(0xFFBAE6FD).copy(alpha = 0.82f)
    )
    private val fogRelativeOffsetsX = floatArrayOf(-0.5f, 0.5f, -0.3f, 0.4f, 0f)
    private val fogRelativeOffsetsY = floatArrayOf(-0.4f, -0.3f, 0.45f, 0.4f, 0f)
    private val fogRelativeRadii = floatArrayOf(0.75f, 0.80f, 0.85f, 0.78f, 0.95f)

    fun drawTray(
        drawScope: DrawScope,
        boardWidth: Int,
        boardHeight: Int,
        tileSize: Float,
        trayPadding: Float = tileSize * 0.15f,
        biome: com.squishout.game.theme.BiomeTheme = com.squishout.game.theme.BiomeTheme.SWEET_MEADOW,
        shockwaveCenter: Offset? = null,
        shockwaveRadius: Float = 0f,
        shockwaveAlpha: Float = 0f
    ) {
        val totalWidth = boardWidth * tileSize + trayPadding * 2
        val totalHeight = boardHeight * tileSize + trayPadding * 2
        val trayRadius = CornerRadius(tileSize * 0.45f, tileSize * 0.45f)

        drawScope.apply {
            // 1. Soft Ambient Drop Shadow under Tray
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.08f),
                topLeft = Offset(0f, tileSize * 0.1f),
                size = Size(totalWidth, totalHeight),
                cornerRadius = trayRadius
            )

            // 2. Glazed Porcelain Enamel Tray Body with Biome Floor
            drawRoundRect(
                color = biome.trayFloor,
                topLeft = Offset.Zero,
                size = Size(totalWidth, totalHeight),
                cornerRadius = trayRadius
            )

            // 3. Subtle Glazed Biome Border
            drawRoundRect(
                color = biome.trayRimDark,
                topLeft = Offset.Zero,
                size = Size(totalWidth, totalHeight),
                cornerRadius = trayRadius,
                style = Stroke(width = tileSize * 0.035f)
            )

            // 4. Recessed Inset Wells for Each Grid Slot
            val wellRadius = tileSize * 0.16f
            val shadowRadius = wellRadius + tileSize * 0.02f
            val shadowOffsetY = tileSize * 0.015f
            val pinDotRadius = tileSize * 0.035f
            val shadowColor = biome.trayRimDark.copy(alpha = 0.25f)
            val rimLight = biome.trayRimLight
            val pinDotColor = biome.trayRimDark.copy(alpha = 0.4f)

            for (x in 0 until boardWidth) {
                val cx = trayPadding + x * tileSize + tileSize / 2f
                for (y in 0 until boardHeight) {
                    val cy = trayPadding + y * tileSize + tileSize / 2f

                    // Soft inner shadow ring
                    drawCircle(
                        color = shadowColor,
                        radius = shadowRadius,
                        center = Offset(cx, cy + shadowOffsetY)
                    )

                    // Well center base
                    drawCircle(
                        color = rimLight,
                        radius = wellRadius,
                        center = Offset(cx, cy)
                    )

                    // Tiny central pin dot
                    drawCircle(
                        color = pinDotColor,
                        radius = pinDotRadius,
                        center = Offset(cx, cy)
                    )
                }
            }

            // 5. Climax Radial Shockwave Ripple (Expanding Ring)
            if (shockwaveRadius > 0f && shockwaveAlpha > 0.01f && shockwaveCenter != null) {
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = shockwaveAlpha * 0.4f),
                    radius = shockwaveRadius,
                    center = shockwaveCenter,
                    style = Stroke(width = tileSize * 0.16f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = shockwaveAlpha * 0.65f),
                    radius = (shockwaveRadius - tileSize * 0.05f).coerceAtLeast(0f),
                    center = shockwaveCenter,
                    style = Stroke(width = tileSize * 0.045f)
                )
            }
        }
    }

    fun drawObstacle(
        drawScope: DrawScope,
        obstacle: Obstacle,
        topLeft: Offset,
        tileSize: Float,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ) {
        val padding = tileSize * 0.08f
        val size = tileSize - padding * 2
        val cornerRadius = CornerRadius(tileSize * 0.28f, tileSize * 0.28f)
        val pos = Offset(topLeft.x + offsetX, topLeft.y + offsetY)

        drawScope.apply {
            val cx = pos.x + size / 2f
            val cy = pos.y + size / 2f

            when (obstacle.type) {
                ObstacleType.ROCK_MOUNTAIN -> {
                    // Rock mountain base
                    drawRoundRect(
                        color = SquishColors.SlateObstacleDark,
                        topLeft = Offset(pos.x, pos.y + tileSize * 0.06f),
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    drawRoundRect(
                        color = SquishColors.SlateObstacle,
                        topLeft = pos,
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    // Mountain triangle
                    val path = scratchMountainPath.apply {
                        reset()
                        moveTo(cx, cy - tileSize * 0.18f)
                        lineTo(cx + tileSize * 0.18f, cy + tileSize * 0.12f)
                        lineTo(cx - tileSize * 0.18f, cy + tileSize * 0.12f)
                        close()
                    }
                    drawPath(path, color = Color.White.copy(alpha = 0.85f), style = Fill)
                }

                ObstacleType.ROCK_TREE -> {
                    // Rock tree base
                    drawRoundRect(
                        color = SquishColors.SlateObstacleDark,
                        topLeft = Offset(pos.x, pos.y + tileSize * 0.06f),
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    drawRoundRect(
                        color = SquishColors.SlateObstacle,
                        topLeft = pos,
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    // Tree symbol
                    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = tileSize * 0.12f, center = Offset(cx, cy - tileSize * 0.05f))
                    drawRect(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = Offset(cx - tileSize * 0.03f, cy + tileSize * 0.05f),
                        size = Size(tileSize * 0.06f, tileSize * 0.1f)
                    )
                }

                ObstacleType.ICE_BLOCK -> {
                    // Frosted Ice Block
                    val iceDark = Color(0xFF60A5FA)
                    val iceBase = Color(0xFF93C5FD)
                    drawRoundRect(
                        color = iceDark,
                        topLeft = Offset(pos.x, pos.y + tileSize * 0.06f),
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    drawRoundRect(
                        color = iceBase,
                        topLeft = pos,
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    // Ice glint highlight
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.6f),
                        topLeft = Offset(pos.x + size * 0.12f, pos.y + size * 0.12f),
                        size = Size(size * 0.76f, size * 0.25f),
                        cornerRadius = CornerRadius(size * 0.12f, size * 0.12f)
                    )

                    // Snowflake / star motif in center
                    val motifColor = Color.White.copy(alpha = 0.9f)
                    drawLine(motifColor, Offset(cx - size * 0.2f, cy), Offset(cx + size * 0.2f, cy), strokeWidth = 3f)
                    drawLine(motifColor, Offset(cx, cy - size * 0.2f), Offset(cx, cy + size * 0.2f), strokeWidth = 3f)
                    drawLine(motifColor, Offset(cx - size * 0.14f, cy - size * 0.14f), Offset(cx + size * 0.14f, cy + size * 0.14f), strokeWidth = 2.5f)
                    drawLine(motifColor, Offset(cx - size * 0.14f, cy + size * 0.14f), Offset(cx + size * 0.14f, cy - size * 0.14f), strokeWidth = 2.5f)

                    // Crack fracture lines if damaged (health < maxHealth)
                    if (obstacle.health < obstacle.maxHealth) {
                        val crackPath = scratchCrackPath.apply {
                            reset()
                            moveTo(cx - size * 0.28f, cy - size * 0.25f)
                            lineTo(cx - size * 0.05f, cy - size * 0.02f)
                            lineTo(cx + size * 0.08f, cy - size * 0.12f)
                            lineTo(cx + size * 0.28f, cy + size * 0.22f)
                        }
                        drawPath(crackPath, color = Color(0xFF1E3A8A), style = Stroke(width = 3.5f))
                    }
                }

                ObstacleType.HONEY_POT -> {
                    // Warm golden Honey Pot
                    val honeyDark = Color(0xFFD97706)
                    val honeyBase = Color(0xFFF59E0B)
                    drawRoundRect(
                        color = honeyDark,
                        topLeft = Offset(pos.x, pos.y + tileSize * 0.06f),
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    drawRoundRect(
                        color = honeyBase,
                        topLeft = pos,
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )

                    // Honey jar rim & dripping honey
                    drawRoundRect(
                        color = Color(0xFFFEF3C7),
                        topLeft = Offset(pos.x + size * 0.15f, pos.y + size * 0.1f),
                        size = Size(size * 0.7f, size * 0.2f),
                        cornerRadius = CornerRadius(size * 0.1f, size * 0.1f)
                    )
                    // Honey drip drop
                    drawCircle(
                        color = Color(0xFFFCD34D),
                        radius = size * 0.14f,
                        center = Offset(cx, cy + size * 0.1f)
                    )
                    // Drip highlight
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = size * 0.05f,
                        center = Offset(cx - size * 0.04f, cy + size * 0.07f)
                    )
                }
            }
        }
    }

    fun drawWaterJet(
        drawScope: DrawScope,
        waterJet: com.squishout.engine.model.WaterJet,
        topLeft: Offset,
        tileSize: Float
    ) {
        val padding = tileSize * 0.08f
        val size = tileSize - padding * 2
        val cx = topLeft.x + size / 2f
        val cy = topLeft.y + size / 2f
        val r = size * 0.44f

        drawScope.apply {
            // 1. Water jet recessed well
            drawCircle(
                color = Color(0xFF0369A1).copy(alpha = 0.25f),
                radius = r + tileSize * 0.03f,
                center = Offset(cx, cy + tileSize * 0.02f)
            )

            // 2. Swirling aqua water disc
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = waterJetColors,
                    center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                    radius = r * 1.1f
                ),
                radius = r,
                center = Offset(cx, cy)
            )

            // 3. Concentric ripple ring
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = r * 0.72f,
                center = Offset(cx, cy),
                style = Stroke(width = tileSize * 0.035f)
            )

            // 4. Directional Conveyor Chevrons pointing in waterJet.direction
            val dir = waterJet.direction
            val arrowLength = r * 0.45f
            val arrowWidth = r * 0.35f

            val tipX = cx + dir.dx * arrowLength * 0.8f
            val tipY = cy + dir.dy * arrowLength * 0.8f
            val baseX = cx - dir.dx * arrowLength * 0.4f
            val baseY = cy - dir.dy * arrowLength * 0.4f
            val perpX = -dir.dy * arrowWidth * 0.7f
            val perpY = dir.dx * arrowWidth * 0.7f

            val arrowPath = scratchArrowPath.apply {
                reset()
                moveTo(tipX, tipY)
                lineTo(baseX + perpX, baseY + perpY)
                lineTo(cx, cy)
                lineTo(baseX - perpX, baseY - perpY)
                close()
            }
            drawPath(arrowPath, color = Color.White, style = androidx.compose.ui.graphics.drawscope.Fill)
        }
    }

    fun drawFogTile(
        drawScope: DrawScope,
        topLeft: Offset,
        tileSize: Float
    ) {
        val padding = tileSize * 0.04f
        val size = tileSize - padding * 2
        val cx = topLeft.x + size / 2f
        val cy = topLeft.y + size / 2f
        val r = size * 0.32f

        drawScope.apply {
            // Cluster of 5 overlapping pearlescent bubbles
            val shadowOffsetY = tileSize * 0.02f
            for (i in 0 until 5) {
                val bubbleCenterX = cx + r * fogRelativeOffsetsX[i]
                val bubbleCenterY = cy + r * fogRelativeOffsetsY[i]
                val radius = r * fogRelativeRadii[i]

                // Soft shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.08f),
                    radius = radius,
                    center = Offset(bubbleCenterX, bubbleCenterY + shadowOffsetY)
                )
                // Pearlescent bubble body
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = fogBubbleColors,
                        center = Offset(bubbleCenterX - radius * 0.3f, bubbleCenterY - radius * 0.3f),
                        radius = radius * 1.1f
                    ),
                    radius = radius,
                    center = Offset(bubbleCenterX, bubbleCenterY)
                )
                // Specular highlight arc
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = radius * 0.28f,
                    center = Offset(bubbleCenterX - radius * 0.35f, bubbleCenterY - radius * 0.35f)
                )
            }

            // Central playful sparkle motif
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.75f),
                radius = r * 0.15f,
                center = Offset(cx, cy)
            )
        }
    }
}

