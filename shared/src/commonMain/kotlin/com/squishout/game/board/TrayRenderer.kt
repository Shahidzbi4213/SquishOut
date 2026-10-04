package com.squishout.game.board

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.squishout.engine.model.Obstacle
import com.squishout.engine.model.ObstacleType
import com.squishout.game.theme.SquishColors

object TrayRenderer {

    fun drawTray(
        drawScope: DrawScope,
        boardWidth: Int,
        boardHeight: Int,
        tileSize: Float,
        trayPadding: Float = tileSize * 0.15f,
        biome: com.squishout.game.theme.BiomeTheme = com.squishout.game.theme.BiomeTheme.SWEET_MEADOW
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
            for (x in 0 until boardWidth) {
                for (y in 0 until boardHeight) {
                    val cx = trayPadding + x * tileSize + tileSize / 2f
                    val cy = trayPadding + y * tileSize + tileSize / 2f
                    val wellRadius = tileSize * 0.16f

                    // Soft inner shadow ring
                    drawCircle(
                        color = biome.trayRimDark.copy(alpha = 0.25f),
                        radius = wellRadius + tileSize * 0.02f,
                        center = Offset(cx, cy + tileSize * 0.015f)
                    )

                    // Well center base
                    drawCircle(
                        color = biome.trayRimLight,
                        radius = wellRadius,
                        center = Offset(cx, cy)
                    )

                    // Tiny central pin dot
                    drawCircle(
                        color = biome.trayRimDark.copy(alpha = 0.4f),
                        radius = tileSize * 0.035f,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }

    fun drawObstacle(
        drawScope: DrawScope,
        obstacle: Obstacle,
        topLeft: Offset,
        tileSize: Float
    ) {
        val padding = tileSize * 0.08f
        val size = tileSize - padding * 2
        val cornerRadius = CornerRadius(tileSize * 0.28f, tileSize * 0.28f)

        drawScope.apply {
            val cx = topLeft.x + size / 2f
            val cy = topLeft.y + size / 2f

            when (obstacle.type) {
                ObstacleType.ROCK_MOUNTAIN -> {
                    // Rock mountain base
                    drawRoundRect(
                        color = SquishColors.SlateObstacleDark,
                        topLeft = Offset(topLeft.x, topLeft.y + tileSize * 0.06f),
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    drawRoundRect(
                        color = SquishColors.SlateObstacle,
                        topLeft = topLeft,
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    // Mountain triangle
                    val path = androidx.compose.ui.graphics.Path().apply {
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
                        topLeft = Offset(topLeft.x, topLeft.y + tileSize * 0.06f),
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    drawRoundRect(
                        color = SquishColors.SlateObstacle,
                        topLeft = topLeft,
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
                        topLeft = Offset(topLeft.x, topLeft.y + tileSize * 0.06f),
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    drawRoundRect(
                        color = iceBase,
                        topLeft = topLeft,
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    // Ice glint highlight
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.6f),
                        topLeft = Offset(topLeft.x + size * 0.12f, topLeft.y + size * 0.12f),
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
                        val crackPath = androidx.compose.ui.graphics.Path().apply {
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
                        topLeft = Offset(topLeft.x, topLeft.y + tileSize * 0.06f),
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )
                    drawRoundRect(
                        color = honeyBase,
                        topLeft = topLeft,
                        size = Size(size, size),
                        cornerRadius = cornerRadius
                    )

                    // Honey jar rim & dripping honey
                    drawRoundRect(
                        color = Color(0xFFFEF3C7),
                        topLeft = Offset(topLeft.x + size * 0.15f, topLeft.y + size * 0.1f),
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
}
