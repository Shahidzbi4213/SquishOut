package com.squishout.game.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

enum class BoosterGlyph {
    UNDO,
    HINT,
    MAGIC_WAND
}

/**
 * 3D Toy-like Booster Dock with concave dish pedestals, rich arcade vector glyphs,
 * and gold-rimmed enamel badges matching the Stitch visual overhaul.
 */
@Composable
fun BoosterDock(
    undoCount: Int,
    hintCount: Int,
    wandCount: Int,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onWand: () -> Unit,
    onShopRequested: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dockShape = RoundedCornerShape(36.dp)

    Box(
        modifier = modifier
            .shadow(8.dp, dockShape)
            .clip(dockShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF6F4EF)
                    )
                )
            )
            .border(1.5.dp, Color(0xFFE6E0D5), dockShape)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoosterPedestal(
                glyph = BoosterGlyph.UNDO,
                badge = undoCount,
                enamelColor = Color(0xFF00897B), // Teal enamel
                onClick = { if (undoCount > 0) onUndo() else onShopRequested() }
            )
            BoosterPedestal(
                glyph = BoosterGlyph.HINT,
                badge = hintCount,
                enamelColor = Color(0xFFE91E63), // Ruby magenta enamel
                onClick = { if (hintCount > 0) onHint() else onShopRequested() }
            )
            BoosterPedestal(
                glyph = BoosterGlyph.MAGIC_WAND,
                badge = wandCount,
                enamelColor = Color(0xFFFB8C00), // Tangerine enamel
                onClick = { if (wandCount > 0) onWand() else onShopRequested() }
            )
        }
    }
}

@Composable
private fun BoosterPedestal(
    glyph: BoosterGlyph,
    badge: Int,
    enamelColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(targetValue = if (isPressed) 2.dp else 0.dp)
    val shadowElevation by animateDpAsState(targetValue = if (isPressed) 1.dp else 4.dp)

    Box(
        modifier = modifier
            .size(62.dp)
            .offset(y = offsetY),
        contentAlignment = Alignment.Center
    ) {
        // 1. Saucer / Dish 3D Pedestal
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(shadowElevation, CircleShape)
                .clip(CircleShape)
                // 3D bottom bevel rim
                .background(Color(0xFFD6CEC4))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Concave dish face
            Box(
                modifier = Modifier
                    .size(58.dp, 55.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF8F5F0),
                                Color(0xFFECE7DE)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFF3EFE8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Vector Glyph
                when (glyph) {
                    BoosterGlyph.UNDO -> UndoClockGlyph()
                    BoosterGlyph.HINT -> HintBulbGlyph()
                    BoosterGlyph.MAGIC_WAND -> MagicWandGlyph()
                }
            }
        }

        // 2. Gold-Bezeled Enamel Badge at top-right
        EnamelBadge(
            count = badge,
            enamelColor = enamelColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 5.dp, y = (-4).dp)
        )
    }
}

@Composable
private fun EnamelBadge(
    count: Int,
    enamelColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFEE58),
                        Color(0xFFFFD54F),
                        Color(0xFFD4AF37),
                        Color(0xFFB8860B)
                    )
                )
            )
            .padding(1.8.dp), // Gold bezel width
        contentAlignment = Alignment.Center
    ) {
        // Enamel core
        Box(
            modifier = Modifier
                .size(20.4.dp)
                .clip(CircleShape)
                .background(enamelColor),
            contentAlignment = Alignment.Center
        ) {
            // Top specular shine crescent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Text(
                text = if (count > 0) "$count" else "+",
                color = Color.White,
                fontSize = if (count > 0) 12.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun UndoClockGlyph() {
    val glyphColor = Color(0xFF7B4724) // Rich warm caramel cocoa
    Canvas(modifier = Modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w * 0.36f
        val strokeWidth = 3.6.dp.toPx()

        // 1. Counter-clockwise rewind arc (sweeping ~280 degrees from 60 to 340)
        drawArc(
            color = glyphColor,
            startAngle = 50f,
            sweepAngle = 275f,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            ),
            topLeft = Offset(cx - radius, cy - radius),
            size = Size(radius * 2, radius * 2)
        )

        // 2. Arrowhead at top left of arc (around angle 325 degrees)
        val angleRad = (325f) * (kotlin.math.PI / 180f).toFloat()
        val arrowTipX = cx + radius * cos(angleRad)
        val arrowTipY = cy + radius * sin(angleRad)

        val arrowPath = Path().apply {
            moveTo(arrowTipX - 3.dp.toPx(), arrowTipY - 7.dp.toPx())
            lineTo(arrowTipX + 6.dp.toPx(), arrowTipY - 2.dp.toPx())
            lineTo(arrowTipX - 1.dp.toPx(), arrowTipY + 6.dp.toPx())
            close()
        }
        drawPath(arrowPath, color = glyphColor, style = Fill)

        // 3. Center clock hands
        // Hour hand pointing up
        drawLine(
            color = glyphColor,
            start = Offset(cx, cy),
            end = Offset(cx, cy - radius * 0.52f),
            strokeWidth = 3.0.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Minute hand pointing right-down
        drawLine(
            color = glyphColor,
            start = Offset(cx, cy),
            end = Offset(cx + radius * 0.44f, cy + radius * 0.12f),
            strokeWidth = 2.6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center hub
        drawCircle(
            color = glyphColor,
            radius = 2.4.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}

@Composable
private fun HintBulbGlyph() {
    Canvas(modifier = Modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.40f
        val bulbRadius = w * 0.26f

        // 1. Warm radial amber glow halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFE082).copy(alpha = 0.60f),
                    Color(0xFFFFCA28).copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = bulbRadius * 1.8f
            ),
            radius = bulbRadius * 1.8f,
            center = Offset(cx, cy)
        )

        // 2. Lightbulb body
        val bulbPath = Path().apply {
            // Round head
            moveTo(cx, cy - bulbRadius)
            cubicTo(
                cx + bulbRadius * 1.1f, cy - bulbRadius,
                cx + bulbRadius * 1.1f, cy + bulbRadius * 0.4f,
                cx + bulbRadius * 0.5f, cy + bulbRadius * 0.85f
            )
            // Neck base
            lineTo(cx + bulbRadius * 0.45f, cy + bulbRadius * 1.25f)
            lineTo(cx - bulbRadius * 0.45f, cy + bulbRadius * 1.25f)
            lineTo(cx - bulbRadius * 0.5f, cy + bulbRadius * 0.85f)
            cubicTo(
                cx - bulbRadius * 1.1f, cy + bulbRadius * 0.4f,
                cx - bulbRadius * 1.1f, cy - bulbRadius,
                cx, cy - bulbRadius
            )
            close()
        }

        // Amber bulb gradient fill
        drawPath(
            path = bulbPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFD54F), // Bright radiant yellow
                    Color(0xFFFFA000)  // Golden amber
                ),
                startY = cy - bulbRadius,
                endY = cy + bulbRadius * 1.25f
            ),
            style = Fill
        )

        // Bulb screw base threads
        val baseTop = cy + bulbRadius * 1.25f
        drawRoundRect(
            color = Color(0xFFC78918),
            topLeft = Offset(cx - bulbRadius * 0.35f, baseTop + 1.dp.toPx()),
            size = Size(bulbRadius * 0.70f, 2.5.dp.toPx()),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFA6690B),
            radius = 2.0.dp.toPx(),
            center = Offset(cx, baseTop + 5.0.dp.toPx())
        )

        // 3. Golden sparkle star at top right of bulb
        drawSparkleStar(
            center = Offset(cx + bulbRadius * 0.95f, cy - bulbRadius * 0.65f),
            size = 4.8.dp.toPx(),
            color = Color(0xFFFFD54F)
        )
    }
}

@Composable
private fun MagicWandGlyph() {
    Canvas(modifier = Modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Draw wand at -45 degree tilt (pointing from bottom-left to top-right)
        rotate(degrees = -45f, pivot = Offset(cx, cy)) {
            val wandLength = w * 0.58f
            val wandThickness = w * 0.18f
            val wandLeft = cx - wandLength / 2f
            val wandTop = cy - wandThickness / 2f

            // Wand purple gradient body
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6A1B9A), // Dark royal violet handle
                        Color(0xFF8E24AA), // Vivid purple
                        Color(0xFFAB47BC)  // Lighter mid
                    )
                ),
                topLeft = Offset(wandLeft, wandTop),
                size = Size(wandLength, wandThickness),
                cornerRadius = CornerRadius(wandThickness / 2f, wandThickness / 2f)
            )

            // Porcelain / Glowing Gold Wand Tip
            val tipWidth = wandLength * 0.28f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFF9C4),
                        Color(0xFFFFE082)
                    )
                ),
                topLeft = Offset(wandLeft + wandLength - tipWidth, wandTop),
                size = Size(tipWidth, wandThickness),
                cornerRadius = CornerRadius(wandThickness / 2f, wandThickness / 2f)
            )
        }

        // Sparkling golden stars around the tip
        drawSparkleStar(
            center = Offset(w * 0.78f, h * 0.22f),
            size = 5.2.dp.toPx(),
            color = Color(0xFFFFD54F)
        )
        drawSparkleStar(
            center = Offset(w * 0.88f, h * 0.48f),
            size = 3.6.dp.toPx(),
            color = Color(0xFFCE93D8)
        )
        drawSparkleStar(
            center = Offset(w * 0.32f, h * 0.20f),
            size = 3.2.dp.toPx(),
            color = Color(0xFFFFD54F)
        )
    }
}

/**
 * Draws an arcade-style 4-point sparkle star with pinched concave bezier curves.
 */
private fun DrawScope.drawSparkleStar(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
        close()
    }
    drawPath(path, color = color, style = Fill)
}
