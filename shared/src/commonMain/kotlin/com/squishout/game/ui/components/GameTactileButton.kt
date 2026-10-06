package com.squishout.game.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class GameIconType {
    BACK,
    SETTINGS
}

/**
 * A tactile, 3D candy-arcade action button with bottom bevel shadow
 * and physical press depression physics matching the Stitch design.
 */
@Composable
fun GameTactileButton(
    iconType: GameIconType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(targetValue = if (isPressed) 2.dp else 0.dp)
    val shadowElevation by animateDpAsState(targetValue = if (isPressed) 1.dp else 4.dp)
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .size(46.dp)
            .offset(y = offsetY)
            .shadow(shadowElevation, shape)
            .clip(shape)
            // 3D bottom rim bevel color
            .background(Color(0xFFD6B588))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Top Face with porcelain gradient
        Box(
            modifier = Modifier
                .size(46.dp, 43.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFDF8),
                            Color(0xFFF7EAD7)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFF0DECA),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when (iconType) {
                GameIconType.BACK -> BackArrowIcon()
                GameIconType.SETTINGS -> SettingsCogIcon()
            }
        }
    }
}

@Composable
private fun BackArrowIcon() {
    val iconColor = Color(0xFF6D4321) // Rich chocolate caramel
    Canvas(modifier = Modifier.size(20.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.62f, size.height * 0.22f)
            lineTo(size.width * 0.35f, size.height * 0.50f)
            lineTo(size.width * 0.62f, size.height * 0.78f)
        }
        drawPath(
            path = path,
            color = iconColor,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
private fun SettingsCogIcon() {
    val iconColor = Color(0xFF6D4321)
    Canvas(modifier = Modifier.size(22.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width * 0.44f
        val innerRadius = size.width * 0.28f
        val toothCount = 6

        // Draw outer gear cogs
        val path = Path()
        for (i in 0 until toothCount) {
            val angle = (i * 360f / toothCount) * (kotlin.math.PI / 180f).toFloat()
            val toothWidthAngle = 18f * (kotlin.math.PI / 180f).toFloat()

            val p1 = Offset(
                center.x + innerRadius * cos(angle - toothWidthAngle),
                center.y + innerRadius * sin(angle - toothWidthAngle)
            )
            val p2 = Offset(
                center.x + outerRadius * cos(angle - toothWidthAngle * 0.6f),
                center.y + outerRadius * sin(angle - toothWidthAngle * 0.6f)
            )
            val p3 = Offset(
                center.x + outerRadius * cos(angle + toothWidthAngle * 0.6f),
                center.y + outerRadius * sin(angle + toothWidthAngle * 0.6f)
            )
            val p4 = Offset(
                center.x + innerRadius * cos(angle + toothWidthAngle),
                center.y + innerRadius * sin(angle + toothWidthAngle)
            )

            if (i == 0) path.moveTo(p1.x, p1.y) else path.lineTo(p1.x, p1.y)
            path.lineTo(p2.x, p2.y)
            path.lineTo(p3.x, p3.y)
            path.lineTo(p4.x, p4.y)
        }
        path.close()

        drawPath(path = path, color = iconColor, style = Fill)

        // Center hub circle
        drawCircle(
            color = iconColor,
            radius = innerRadius,
            center = center
        )

        // Center bore cutout (cream background color)
        drawCircle(
            color = Color(0xFFF7EAD7),
            radius = size.width * 0.12f,
            center = center
        )
    }
}
