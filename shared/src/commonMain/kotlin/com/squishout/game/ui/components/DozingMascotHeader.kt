package com.squishout.game.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.theme.SlateCharcoal

/**
 * A charming animated mascot header for the Pause/Settings modal.
 * Features a sleepy dozing Strawberry Blob taking a nap with gentle breathing,
 * soft rosy blush, and floating "Zzz" dream bubbles.
 */
@Composable
fun DozingMascotHeader(
    stage: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DozingTransition")

    // Gentle breathing scale oscillation
    val breathScaleY by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathY"
    )
    val breathScaleX by infiniteTransition.animateFloat(
        initialValue = 1.03f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathX"
    )

    // Floating Zzz offset and alpha
    val zzzFloat1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Zzz1"
    )
    val zzzAlpha1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ZzzAlpha1"
    )

    val zzzFloat2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -28f,
        animationSpec = infiniteRepeatable(
            animation = tween(3100, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Zzz2"
    )
    val zzzAlpha2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3100, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ZzzAlpha2"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Mascot Container with Zzz floating particles
        Box(
            modifier = Modifier.size(92.dp),
            contentAlignment = Alignment.Center
        ) {
            // Soft Radial Pedestal Shadow underneath
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 64.dp, height = 16.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFD6B588).copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Dozing Strawberry Mascot
            Canvas(
                modifier = Modifier
                    .size(68.dp)
                    .scale(scaleX = breathScaleX, scaleY = breathScaleY)
            ) {
                val center = Offset(size.width / 2f, size.height * 0.54f)
                val bodyRadius = size.width * 0.40f

                // 1. Plump Strawberry Gelatin Body with Multi-Stop Radial Gloss
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF85A1),
                            Color(0xFFFB7185),
                            Color(0xFFE11D48),
                            Color(0xFF9F1239)
                        ),
                        center = Offset(center.x - bodyRadius * 0.25f, center.y - bodyRadius * 0.25f),
                        radius = bodyRadius * 1.35f
                    ),
                    radius = bodyRadius,
                    center = center
                )

                // 2. Translucent Specular Glaze Highlight (Crescent Arc)
                val highlightPath = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            center.x - bodyRadius * 0.65f,
                            center.y - bodyRadius * 0.75f,
                            center.x - bodyRadius * 0.05f,
                            center.y - bodyRadius * 0.25f
                        )
                    )
                }
                drawPath(
                    path = highlightPath,
                    color = Color.White.copy(alpha = 0.65f),
                    style = Fill
                )

                // 3. Green Strawberry Calyx Leaf Crown on Top
                val leafPath = Path().apply {
                    val topY = center.y - bodyRadius * 0.88f
                    moveTo(center.x, topY)
                    // Center leaf
                    quadraticTo(center.x, topY - 10.dp.toPx(), center.x + 3.dp.toPx(), topY - 14.dp.toPx())
                    quadraticTo(center.x + 5.dp.toPx(), topY - 8.dp.toPx(), center.x + 6.dp.toPx(), topY)
                    // Right leaf
                    quadraticTo(center.x + 14.dp.toPx(), topY - 4.dp.toPx(), center.x + 16.dp.toPx(), topY + 4.dp.toPx())
                    quadraticTo(center.x + 8.dp.toPx(), topY + 4.dp.toPx(), center.x + 2.dp.toPx(), topY + 3.dp.toPx())
                    // Left leaf
                    quadraticTo(center.x - 12.dp.toPx(), topY + 4.dp.toPx(), center.x - 16.dp.toPx(), topY - 2.dp.toPx())
                    quadraticTo(center.x - 8.dp.toPx(), topY - 6.dp.toPx(), center.x, topY)
                    close()
                }
                drawPath(
                    path = leafPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A))
                    )
                )

                // 4. Soft Rosy Blush Cheeks
                val cheekRadius = bodyRadius * 0.18f
                drawCircle(
                    color = Color(0xFFFFB4C8).copy(alpha = 0.70f),
                    radius = cheekRadius,
                    center = Offset(center.x - bodyRadius * 0.50f, center.y + bodyRadius * 0.18f)
                )
                drawCircle(
                    color = Color(0xFFFFB4C8).copy(alpha = 0.70f),
                    radius = cheekRadius,
                    center = Offset(center.x + bodyRadius * 0.50f, center.y + bodyRadius * 0.18f)
                )

                // 5. Closed Sleeping Curved Eyelids ( ˘ ◡ ˘ )
                val eyeWidth = bodyRadius * 0.28f
                val eyeY = center.y + bodyRadius * 0.05f

                // Left sleeping eye curve
                val leftEyePath = Path().apply {
                    moveTo(center.x - bodyRadius * 0.46f, eyeY)
                    quadraticTo(
                        center.x - bodyRadius * 0.32f,
                        eyeY + 5.dp.toPx(),
                        center.x - bodyRadius * 0.18f,
                        eyeY
                    )
                }
                drawPath(
                    path = leftEyePath,
                    color = Color(0xFF4A1E29),
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Right sleeping eye curve
                val rightEyePath = Path().apply {
                    moveTo(center.x + bodyRadius * 0.18f, eyeY)
                    quadraticTo(
                        center.x + bodyRadius * 0.32f,
                        eyeY + 5.dp.toPx(),
                        center.x + bodyRadius * 0.46f,
                        eyeY
                    )
                }
                drawPath(
                    path = rightEyePath,
                    color = Color(0xFF4A1E29),
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Tiny peaceful smile
                val smilePath = Path().apply {
                    moveTo(center.x - 3.dp.toPx(), center.y + bodyRadius * 0.34f)
                    quadraticTo(
                        center.x,
                        center.y + bodyRadius * 0.44f,
                        center.x + 3.dp.toPx(),
                        center.y + bodyRadius * 0.34f
                    )
                }
                drawPath(
                    path = smilePath,
                    color = Color(0xFF4A1E29),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Floating "Z" particles
            Text(
                text = "z",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF9333EA).copy(alpha = zzzAlpha1),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = (4 + zzzFloat1).dp)
            )
            Text(
                text = "Z",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFA855F7).copy(alpha = zzzAlpha2),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-4 + zzzFloat2).dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3D Candy Headline with Warm Relief Drop Shadow
        Text(
            text = "PAUSED",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = SlateCharcoal,
            letterSpacing = 1.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0x33D6B588),
                    offset = Offset(0f, 3f),
                    blurRadius = 2f
                )
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Stage & Encouragement Pill
        Box(
            modifier = Modifier
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3ECE0))
                .padding(horizontal = 12.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Stage $stage • Catching a breath!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D5B28)
            )
        }
    }
}
