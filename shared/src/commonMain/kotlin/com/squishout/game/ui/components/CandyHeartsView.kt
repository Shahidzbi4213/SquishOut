package com.squishout.game.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/**
 * 3D Candy Hearts HUD view matching the Stitch game design.
 * Displays plump, juicy ruby candy hearts with specular white highlights
 * housed in a frosted translucent pill tray.
 */
@Composable
fun CandyHeartsView(
    hearts: Int,
    maxHearts: Int = 3,
    modifier: Modifier = Modifier
) {
    val trayShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .shadow(2.dp, trayShape)
            .clip(trayShape)
            .background(Color(0xFFE8EEF5).copy(alpha = 0.88f))
            .border(1.dp, Color(0xFFD0DBE5), trayShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..maxHearts) {
                CandyHeartItem(isFilled = i <= hearts)
            }
        }
    }
}

@Composable
private fun CandyHeartItem(isFilled: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.88f,
        animationSpec = tween(durationMillis = 250)
    )

    Canvas(
        modifier = Modifier
            .size(width = 25.dp, height = 23.dp)
            .scale(scale)
    ) {
        val w = size.width
        val h = size.height

        // Symmetrical, plump cartoon candy heart path
        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.88f)
            // Left lobe
            cubicTo(
                w * 0.08f, h * 0.58f,
                -w * 0.04f, h * 0.22f,
                w * 0.24f, h * 0.08f
            )
            cubicTo(
                w * 0.38f, h * -0.01f,
                w * 0.50f, h * 0.16f,
                w * 0.50f, h * 0.24f
            )
            // Right lobe
            cubicTo(
                w * 0.50f, h * 0.16f,
                w * 0.62f, h * -0.01f,
                w * 0.76f, h * 0.08f
            )
            cubicTo(
                w * 1.04f, h * 0.22f,
                w * 0.92f, h * 0.58f,
                w * 0.5f, h * 0.88f
            )
            close()
        }

        if (isFilled) {
            // 1. Bottom bevel shadow
            drawPath(
                path = heartPath,
                color = Color(0xFF9E0B42),
                style = Fill
            )

            // 2. Rich candy gradient fill
            val rubyGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF3377), // Top vivid ruby highlight
                    Color(0xFFE6195E), // Mid rich pink
                    Color(0xFFB50E45)  // Bottom depth
                ),
                startY = 0f,
                endY = h
            )
            drawPath(
                path = heartPath,
                brush = rubyGradient,
                style = Fill
            )

            // 3. Crisp white candy specular reflection pill on top-left lobe
            rotate(degrees = -30f, pivot = Offset(w * 0.30f, h * 0.25f)) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.90f),
                    topLeft = Offset(w * 0.20f, h * 0.18f),
                    size = Size(w * 0.24f, h * 0.14f),
                    cornerRadius = CornerRadius(h * 0.07f, h * 0.07f)
                )
            }

            // 4. Secondary tiny gleam dot
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = w * 0.045f,
                center = Offset(w * 0.72f, h * 0.28f)
            )
        } else {
            // Depleted frosted sugar shell
            drawPath(
                path = heartPath,
                color = Color.White.copy(alpha = 0.35f),
                style = Fill
            )
            drawPath(
                path = heartPath,
                color = Color(0xFFBAC7D5),
                style = Stroke(
                    width = 1.6.dp.toPx()
                )
            )
        }
    }
}
