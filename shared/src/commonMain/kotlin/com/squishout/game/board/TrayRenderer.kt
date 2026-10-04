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
        trayPadding: Float = tileSize * 0.15f
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

            // 2. Glazed Porcelain Enamel Tray Body
            drawRoundRect(
                color = SquishColors.PorcelainTray,
                topLeft = Offset.Zero,
                size = Size(totalWidth, totalHeight),
                cornerRadius = trayRadius
            )

            // 3. Subtle Glazed Border
            drawRoundRect(
                color = SquishColors.PorcelainTrayBorder,
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
                        color = SquishColors.InsetWellShadow.copy(alpha = 0.65f),
                        radius = wellRadius + tileSize * 0.02f,
                        center = Offset(cx, cy + tileSize * 0.015f)
                    )

                    // Well center base
                    drawCircle(
                        color = SquishColors.InsetWell,
                        radius = wellRadius,
                        center = Offset(cx, cy)
                    )

                    // Tiny central pin dot
                    drawCircle(
                        color = SquishColors.PorcelainTrayBorder,
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
            // Rock base
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

            // Distinct glyph indicator (Mountain vs Tree)
            val cx = topLeft.x + size / 2f
            val cy = topLeft.y + size / 2f
            val glyphColor = Color.White.copy(alpha = 0.85f)

            if (obstacle.type == ObstacleType.ROCK_MOUNTAIN) {
                // Draw mountain triangle
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx, cy - tileSize * 0.18f)
                    lineTo(cx + tileSize * 0.18f, cy + tileSize * 0.12f)
                    lineTo(cx - tileSize * 0.18f, cy + tileSize * 0.12f)
                    close()
                }
                drawPath(path, color = glyphColor, style = Fill)
            } else {
                // Draw tree symbol (circle + trunk)
                drawCircle(color = glyphColor, radius = tileSize * 0.12f, center = Offset(cx, cy - tileSize * 0.05f))
                drawRect(
                    color = glyphColor,
                    topLeft = Offset(cx - tileSize * 0.03f, cy + tileSize * 0.05f),
                    size = Size(tileSize * 0.06f, tileSize * 0.1f)
                )
            }
        }
    }
}
