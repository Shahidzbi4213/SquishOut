package com.squishout.game.ui.components

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

enum class ModalButtonVariant {
    EMERALD, // Primary Action (Resume / Next Level)
    CREAM,   // Secondary Action (Restart)
    CORAL    // Exit / Danger
}

/**
 * 3D Tactile extruded push button for arcade modals with physical press depression,
 * bottom shadow bevel, and custom Skia vector icons.
 */
@Composable
fun ModalExtrudedButton(
    text: String,
    variant: ModalButtonVariant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(targetValue = if (isPressed) 3.dp else 0.dp)
    val shadowElevation by animateDpAsState(targetValue = if (isPressed) 1.dp else 4.dp)
    val cornerRadius = 24.dp
    val buttonShape = RoundedCornerShape(cornerRadius)

    val (topColors, bevelColor, borderColor, textColor, textShadow) = when (variant) {
        ModalButtonVariant.EMERALD -> Quintuple(
            listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669)),
            Color(0xFF047857),
            Color(0xFF6EE7B7).copy(alpha = 0.6f),
            Color.White,
            Color(0x66003B2C)
        )
        ModalButtonVariant.CREAM -> Quintuple(
            listOf(Color(0xFFFFFDF8), Color(0xFFF7EAD7), Color(0xFFEEDBC5)),
            Color(0xFFD6B588),
            Color(0xFFF0DECA),
            Color(0xFF6D4321),
            Color(0x33D6B588)
        )
        ModalButtonVariant.CORAL -> Quintuple(
            listOf(Color(0xFFFFF1F2), Color(0xFFFEE2E2), Color(0xFFFECACA)),
            Color(0xFFF87171),
            Color(0xFFFECDD3),
            Color(0xFFB91C1C),
            Color(0x33F87171)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .offset(y = offsetY)
            .shadow(shadowElevation, buttonShape)
            .clip(buttonShape)
            .background(bevelColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Top Face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Brush.verticalGradient(topColors))
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = 20.dp, bottomEnd = 20.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = 0.6.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = textShadow,
                            offset = Offset(0f, 1.5f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

// Bespoke Canvas Icons for Modal Buttons
@Composable
fun ModalPlayIcon(color: Color = Color.White) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.20f, size.height * 0.15f)
            lineTo(size.width * 0.88f, size.height * 0.50f)
            lineTo(size.width * 0.20f, size.height * 0.85f)
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}

@Composable
fun ModalRestartIcon(color: Color = Color(0xFF6D4321)) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.38f

        // Circular arc
        val arcPath = Path().apply {
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    center.x - radius,
                    center.y - radius,
                    center.x + radius,
                    center.y + radius
                ),
                startAngleDegrees = 30f,
                sweepAngleDegrees = 280f,
                forceMoveTo = false
            )
        }
        drawPath(
            path = arcPath,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Arrow tip at start of arc
        val arrowPath = Path().apply {
            val arrowX = center.x + radius * cos(30f * (kotlin.math.PI / 180f).toFloat())
            val arrowY = center.y + radius * sin(30f * (kotlin.math.PI / 180f).toFloat())
            moveTo(arrowX - 4.dp.toPx(), arrowY - 6.dp.toPx())
            lineTo(arrowX + 2.dp.toPx(), arrowY)
            lineTo(arrowX - 4.dp.toPx(), arrowY + 6.dp.toPx())
        }
        drawPath(path = arrowPath, color = color, style = Fill)
    }
}

@Composable
fun ModalMapIcon(color: Color = Color(0xFFB91C1C)) {
    Canvas(modifier = Modifier.size(17.dp)) {
        val w = size.width
        val h = size.height

        // 3-panel folded map outline
        val path = Path().apply {
            moveTo(w * 0.10f, h * 0.20f)
            lineTo(w * 0.38f, h * 0.10f)
            lineTo(w * 0.65f, h * 0.22f)
            lineTo(w * 0.90f, h * 0.12f)
            lineTo(w * 0.90f, h * 0.80f)
            lineTo(w * 0.65f, h * 0.90f)
            lineTo(w * 0.38f, h * 0.78f)
            lineTo(w * 0.10f, h * 0.88f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Fold lines
        drawLine(
            color = color,
            start = Offset(w * 0.38f, h * 0.10f),
            end = Offset(w * 0.38f, h * 0.78f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.65f, h * 0.22f),
            end = Offset(w * 0.65f, h * 0.90f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun SoundSpeakerIcon(color: Color = Color(0xFF6D4321)) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        // Speaker cone
        val cone = Path().apply {
            moveTo(w * 0.15f, h * 0.36f)
            lineTo(w * 0.36f, h * 0.36f)
            lineTo(w * 0.60f, h * 0.18f)
            lineTo(w * 0.60f, h * 0.82f)
            lineTo(w * 0.36f, h * 0.64f)
            lineTo(w * 0.15f, h * 0.64f)
            close()
        }
        drawPath(path = cone, color = color, style = Fill)

        // Sound wave arcs
        val arc1 = Path().apply {
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(w * 0.50f, h * 0.32f, w * 0.78f, h * 0.68f),
                startAngleDegrees = -45f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        }
        drawPath(path = arc1, color = color, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

        val arc2 = Path().apply {
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(w * 0.62f, h * 0.18f, w * 0.96f, h * 0.82f),
                startAngleDegrees = -45f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        }
        drawPath(path = arc2, color = color, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun VibrateWaveIcon(color: Color = Color(0xFF6D4321)) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        // Center Phone rectangle
        val phoneRect = androidx.compose.ui.geometry.RoundRect(
            rect = androidx.compose.ui.geometry.Rect(w * 0.32f, h * 0.14f, w * 0.68f, h * 0.86f),
            radiusX = 3.dp.toPx(),
            radiusY = 3.dp.toPx()
        )
        val phonePath = Path().apply { addRoundRect(phoneRect) }
        drawPath(path = phonePath, color = color, style = Stroke(width = 2.dp.toPx()))

        // Left vibration brackets
        val leftWave = Path().apply {
            moveTo(w * 0.18f, h * 0.30f)
            lineTo(w * 0.10f, h * 0.50f)
            lineTo(w * 0.18f, h * 0.70f)
        }
        drawPath(path = leftWave, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        // Right vibration brackets
        val rightWave = Path().apply {
            moveTo(w * 0.82f, h * 0.30f)
            lineTo(w * 0.90f, h * 0.50f)
            lineTo(w * 0.82f, h * 0.70f)
        }
        drawPath(path = rightWave, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun MusicNoteIcon(color: Color = Color(0xFF6D4321)) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height

        // Left Note Head
        drawOval(
            color = color,
            topLeft = Offset(w * 0.12f, h * 0.60f),
            size = Size(w * 0.30f, h * 0.24f)
        )

        // Right Note Head
        drawOval(
            color = color,
            topLeft = Offset(w * 0.58f, h * 0.46f),
            size = Size(w * 0.30f, h * 0.24f)
        )

        // Left Stem
        drawLine(
            color = color,
            start = Offset(w * 0.38f, h * 0.68f),
            end = Offset(w * 0.38f, h * 0.18f),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Right Stem
        drawLine(
            color = color,
            start = Offset(w * 0.84f, h * 0.54f),
            end = Offset(w * 0.84f, h * 0.08f),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Connecting Beam
        val beamPath = Path().apply {
            moveTo(w * 0.36f, h * 0.18f)
            lineTo(w * 0.86f, h * 0.08f)
            lineTo(w * 0.86f, h * 0.20f)
            lineTo(w * 0.36f, h * 0.30f)
            close()
        }
        drawPath(beamPath, color = color, style = Fill)
    }
}
