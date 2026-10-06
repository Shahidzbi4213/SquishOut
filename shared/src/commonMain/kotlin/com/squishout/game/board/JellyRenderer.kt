package com.squishout.game.board

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.squishout.engine.model.Direction
import com.squishout.engine.model.EyeState
import com.squishout.engine.model.Jelly
import com.squishout.engine.model.JellyType
import com.squishout.game.theme.SquishColors

object JellyRenderer {

    fun drawJelly(
        drawScope: DrawScope,
        jelly: Jelly,
        topLeft: Offset,
        tileSize: Float,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        alpha: Float = 1f,
        isHighlighted: Boolean = false
    ) {
        val (baseColor, lightColor, darkColor) = getColorPalette(jelly.type)
        val padding = tileSize * 0.08f

        // Calculate bounding box based on occupied tiles
        val minX = jelly.tiles.minOf { it.x }
        val maxX = jelly.tiles.maxOf { it.x }
        val minY = jelly.tiles.minOf { it.y }
        val maxY = jelly.tiles.maxOf { it.y }

        val width = (maxX - minX + 1) * tileSize - padding * 2
        val height = (maxY - minY + 1) * tileSize - padding * 2

        val cx = topLeft.x + width / 2f + offsetX
        val cy = topLeft.y + height / 2f + offsetY

        drawScope.apply {
            val adjustedWidth = width * scaleX
            val adjustedHeight = height * scaleY
            val rectLeft = cx - adjustedWidth / 2f
            val rectTop = cy - adjustedHeight / 2f
            val cornerRadius = CornerRadius(tileSize * 0.38f, tileSize * 0.38f)

            // 1. Soft Drop Shadow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.12f * alpha),
                topLeft = Offset(rectLeft, rectTop + tileSize * 0.08f),
                size = Size(adjustedWidth, adjustedHeight),
                cornerRadius = cornerRadius
            )

            // 2. Gummy Gradient Fill
            val bodyBrush = Brush.radialGradient(
                colors = listOf(lightColor.copy(alpha = alpha), baseColor.copy(alpha = alpha), darkColor.copy(alpha = alpha)),
                center = Offset(rectLeft + adjustedWidth * 0.4f, rectTop + adjustedHeight * 0.35f),
                radius = adjustedWidth * 0.8f
            )

            drawRoundRect(
                brush = bodyBrush,
                topLeft = Offset(rectLeft, rectTop),
                size = Size(adjustedWidth, adjustedHeight),
                cornerRadius = cornerRadius
            )

            // 3. Directional Snout / Sprout
            drawDirectionalSnout(this, jelly.direction, rectLeft, rectTop, adjustedWidth, adjustedHeight, tileSize, baseColor, alpha)

            // 4. Awake Sparkling Rim Glow
            if (jelly.eyeState == EyeState.AWAKE) {
                val rimColor = if (isHighlighted) SquishColors.StarGold else Color.White
                drawRoundRect(
                    color = rimColor.copy(alpha = 0.75f * alpha),
                    topLeft = Offset(rectLeft, rectTop),
                    size = Size(adjustedWidth, adjustedHeight),
                    cornerRadius = cornerRadius,
                    style = Stroke(width = tileSize * 0.05f)
                )
            }

            // 5. Specular Highlights (Edible Gloss)
            val highlightRadius = CornerRadius(adjustedWidth * 0.2f, adjustedHeight * 0.15f)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.55f * alpha),
                topLeft = Offset(rectLeft + adjustedWidth * 0.15f, rectTop + adjustedHeight * 0.12f),
                size = Size(adjustedWidth * 0.35f, adjustedHeight * 0.22f),
                cornerRadius = highlightRadius
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.45f * alpha),
                radius = adjustedWidth * 0.05f,
                center = Offset(rectLeft + adjustedWidth * 0.6f, rectTop + adjustedHeight * 0.18f)
            )

            // 6. Expressive Eyes and Mouth
            drawFace(this, jelly.eyeState, jelly.direction, cx, cy, tileSize, alpha)
        }
    }

    private fun drawDirectionalSnout(
        drawScope: DrawScope,
        direction: Direction,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        tileSize: Float,
        color: Color,
        alpha: Float
    ) {
        val snoutPath = Path()
        val snoutSize = tileSize * 0.22f

        when (direction) {
            Direction.NORTH -> {
                // Crown bump pointing UP
                snoutPath.moveTo(left + width * 0.35f, top + tileSize * 0.05f)
                snoutPath.quadraticTo(left + width * 0.5f, top - snoutSize * 0.8f, left + width * 0.65f, top + tileSize * 0.05f)
                snoutPath.close()
            }
            Direction.EAST -> {
                // Droplet nose pointing RIGHT
                snoutPath.moveTo(left + width - tileSize * 0.05f, top + height * 0.35f)
                snoutPath.quadraticTo(left + width + snoutSize * 0.8f, top + height * 0.5f, left + width - tileSize * 0.05f, top + height * 0.65f)
                snoutPath.close()
            }
            Direction.WEST -> {
                // Droplet nose pointing LEFT
                snoutPath.moveTo(left + tileSize * 0.05f, top + height * 0.35f)
                snoutPath.quadraticTo(left - snoutSize * 0.8f, top + height * 0.5f, left + tileSize * 0.05f, top + height * 0.65f)
                snoutPath.close()
            }
            Direction.SOUTH -> {
                // Tapered bump pointing DOWN
                snoutPath.moveTo(left + width * 0.35f, top + height - tileSize * 0.05f)
                snoutPath.quadraticTo(left + width * 0.5f, top + height + snoutSize * 0.8f, left + width * 0.65f, top + height - tileSize * 0.05f)
                snoutPath.close()
            }
        }

        drawScope.drawPath(snoutPath, color = color.copy(alpha = alpha), style = Fill)
    }

    private fun drawFace(
        drawScope: DrawScope,
        eyeState: EyeState,
        direction: Direction,
        cx: Float,
        cy: Float,
        tileSize: Float,
        alpha: Float
    ) {
        // Shift eye center slightly toward the direction of travel
        val eyeShiftX = direction.dx * tileSize * 0.07f
        val eyeShiftY = direction.dy * tileSize * 0.07f
        val faceCenterX = cx + eyeShiftX
        val faceCenterY = cy + eyeShiftY

        val eyeSpacing = tileSize * 0.22f
        val eyeY = faceCenterY - tileSize * 0.04f
        val eyeRadius = tileSize * 0.075f
        val darkCharcoal = Color(0xFF261C2C).copy(alpha = alpha)

        if (eyeState == EyeState.AWAKE) {
            // Sparkling wide awake eyes ( ✦‿✦ )
            // Left eye
            drawScope.drawCircle(color = darkCharcoal, radius = eyeRadius, center = Offset(faceCenterX - eyeSpacing, eyeY))
            drawScope.drawCircle(color = Color.White.copy(alpha = alpha), radius = eyeRadius * 0.45f, center = Offset(faceCenterX - eyeSpacing - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))

            // Right eye
            drawScope.drawCircle(color = darkCharcoal, radius = eyeRadius, center = Offset(faceCenterX + eyeSpacing, eyeY))
            drawScope.drawCircle(color = Color.White.copy(alpha = alpha), radius = eyeRadius * 0.45f, center = Offset(faceCenterX + eyeSpacing - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))

            // Happy smile curve
            val mouthPath = Path().apply {
                moveTo(faceCenterX - tileSize * 0.08f, faceCenterY + tileSize * 0.1f)
                quadraticTo(faceCenterX, faceCenterY + tileSize * 0.18f, faceCenterX + tileSize * 0.08f, faceCenterY + tileSize * 0.1f)
            }
            drawScope.drawPath(mouthPath, color = darkCharcoal, style = Stroke(width = tileSize * 0.04f, cap = StrokeCap.Round))
        } else {
            // Peaceful sleeping arcs ( ˘◡˘ )
            val leftArc = Path().apply {
                moveTo(faceCenterX - eyeSpacing - eyeRadius, eyeY)
                quadraticTo(faceCenterX - eyeSpacing, eyeY - eyeRadius * 0.8f, faceCenterX - eyeSpacing + eyeRadius, eyeY)
            }
            val rightArc = Path().apply {
                moveTo(faceCenterX + eyeSpacing - eyeRadius, eyeY)
                quadraticTo(faceCenterX + eyeSpacing, eyeY - eyeRadius * 0.8f, faceCenterX + eyeSpacing + eyeRadius, eyeY)
            }

            drawScope.drawPath(leftArc, color = darkCharcoal.copy(alpha = 0.7f * alpha), style = Stroke(width = tileSize * 0.04f, cap = StrokeCap.Round))
            drawScope.drawPath(rightArc, color = darkCharcoal.copy(alpha = 0.7f * alpha), style = Stroke(width = tileSize * 0.04f, cap = StrokeCap.Round))

            // Calm slight resting smile
            val calmMouth = Path().apply {
                moveTo(faceCenterX - tileSize * 0.05f, faceCenterY + tileSize * 0.12f)
                quadraticTo(faceCenterX, faceCenterY + tileSize * 0.15f, faceCenterX + tileSize * 0.05f, faceCenterY + tileSize * 0.12f)
            }
            drawScope.drawPath(calmMouth, color = darkCharcoal.copy(alpha = 0.6f * alpha), style = Stroke(width = tileSize * 0.035f, cap = StrokeCap.Round))
        }
    }

    fun drawCandyTether(
        drawScope: DrawScope,
        p1: Offset,
        p2: Offset,
        tileSize: Float,
        isAwake: Boolean = false
    ) {
        drawScope.apply {
            val tetherWidth = tileSize * 0.16f
            val shadowOffset = Offset(0f, tileSize * 0.04f)

            // 1. Soft Shadow
            drawLine(
                color = Color.Black.copy(alpha = 0.12f),
                start = p1 + shadowOffset,
                end = p2 + shadowOffset,
                strokeWidth = tetherWidth,
                cap = StrokeCap.Round
            )

            // 2. Gummy Candy Ribbon
            val ribbonColor = if (isAwake) Color(0xFFFBBF24) else Color(0xFFF472B6)
            val ribbonBorder = if (isAwake) Color(0xFFD97706) else Color(0xFFBE123C)

            drawLine(
                color = ribbonBorder,
                start = p1,
                end = p2,
                strokeWidth = tetherWidth + tileSize * 0.04f,
                cap = StrokeCap.Round
            )

            drawLine(
                color = ribbonColor,
                start = p1,
                end = p2,
                strokeWidth = tetherWidth,
                cap = StrokeCap.Round
            )

            // 3. Central Candy Heart Emblem
            val mid = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
            val heartRadius = tileSize * 0.14f

            drawCircle(
                color = Color.White,
                radius = heartRadius + 2f,
                center = mid
            )
            drawCircle(
                color = if (isAwake) Color(0xFFF59E0B) else Color(0xFFE11D48),
                radius = heartRadius,
                center = mid
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = heartRadius * 0.35f,
                center = Offset(mid.x - heartRadius * 0.3f, mid.y - heartRadius * 0.3f)
            )
        }
    }

    private fun getColorPalette(type: JellyType): Triple<Color, Color, Color> =
        when (type) {
            JellyType.STRAWBERRY -> Triple(SquishColors.Strawberry, SquishColors.StrawberryLight, SquishColors.StrawberryDark)
            JellyType.BLUEBERRY -> Triple(SquishColors.Blueberry, SquishColors.BlueberryLight, SquishColors.BlueberryDark)
            JellyType.LEMON -> Triple(SquishColors.Lemon, SquishColors.LemonLight, SquishColors.LemonDark)
            JellyType.KIWI -> Triple(SquishColors.Kiwi, SquishColors.KiwiLight, SquishColors.KiwiDark)
            JellyType.GRAPE_EEL -> Triple(SquishColors.Grape, SquishColors.GrapeLight, SquishColors.GrapeDark)
        }
}
