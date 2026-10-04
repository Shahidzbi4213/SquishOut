package com.squishout.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.theme.AmberGlow
import com.squishout.game.theme.BlueberryBase
import com.squishout.game.theme.BlueberryGloss
import com.squishout.game.theme.CoralHeart
import com.squishout.game.theme.EmeraldMint
import com.squishout.game.theme.FrostedWhite
import com.squishout.game.theme.SlateCharcoal

@Composable
fun GameOverModal(
    isVisible: Boolean,
    onReviveWithAd: () -> Unit,
    onReviveWithGems: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(250)) + scaleIn(tween(300, easing = FastOutSlowInEasing), initialScale = 0.85f),
        exit = fadeOut(tween(200)) + scaleOut(tween(250), targetScale = 0.85f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = FrostedWhite),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .shadow(24.dp, RoundedCornerShape(32.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    // Sad Mascot Canvas
                    SadMascotBlob(modifier = Modifier.size(110.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "OUT OF HEARTS!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = CoralHeart,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "The jellies got squished!\nDon't leave them trapped in the tray!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateCharcoal.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3 Empty Hearts
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🤍", fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Ad Revive Button (+3 Hearts)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(28.dp), spotColor = EmeraldMint)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF10B981), Color(0xFF059669))
                                )
                            )
                            .clickable { onReviveWithAd() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "▶", fontSize = 16.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "CONTINUE WITH +3 ❤️",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "FREE REWARDED AD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gem Revive Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(6.dp, RoundedCornerShape(25.dp), spotColor = AmberGlow)
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                )
                            )
                            .clickable { onReviveWithGems() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "💎", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "10 GEMS TO REVIVE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Restart Level CTA
                    Text(
                        text = "⟲ Give Up & Restart",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateCharcoal.copy(alpha = 0.55f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRestart() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SadMascotBlob(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val tearDrop by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width * 0.42f

        // Shadow beneath
        drawOval(
            color = Color.Black.copy(alpha = 0.12f),
            topLeft = Offset(cx - radius * 0.85f, cy + radius * 0.65f),
            size = Size(radius * 1.7f, radius * 0.35f)
        )

        // Radial gummy body
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(BlueberryGloss, BlueberryBase, Color(0xFF1E3A8A)),
                center = Offset(cx - radius * 0.25f, cy - radius * 0.25f),
                radius = radius * 1.2f
            ),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Specular highlight arc
        drawOval(
            color = Color.White.copy(alpha = 0.65f),
            topLeft = Offset(cx - radius * 0.6f, cy - radius * 0.75f),
            size = Size(radius * 0.8f, radius * 0.35f)
        )

        // Sad droopy eyes (large puppy eyes with glistens)
        val eyeRadius = radius * 0.22f
        val leftEyeX = cx - radius * 0.38f
        val rightEyeX = cx + radius * 0.38f
        val eyeY = cy - radius * 0.05f

        // Left eye
        drawCircle(color = Color(0xFF0F172A), radius = eyeRadius, center = Offset(leftEyeX, eyeY))
        drawCircle(color = Color.White, radius = eyeRadius * 0.45f, center = Offset(leftEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))
        drawCircle(color = Color.White, radius = eyeRadius * 0.2f, center = Offset(leftEyeX + eyeRadius * 0.3f, eyeY + eyeRadius * 0.3f))

        // Right eye
        drawCircle(color = Color(0xFF0F172A), radius = eyeRadius, center = Offset(rightEyeX, eyeY))
        drawCircle(color = Color.White, radius = eyeRadius * 0.45f, center = Offset(rightEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))
        drawCircle(color = Color.White, radius = eyeRadius * 0.2f, center = Offset(rightEyeX + eyeRadius * 0.3f, eyeY + eyeRadius * 0.3f))

        // Sad mouth arc
        drawSadMouth(cx, cy + radius * 0.3f, radius * 0.22f)

        // Animated tear rolling down right cheek
        val tearY = eyeY + eyeRadius + (tearDrop * radius * 0.5f)
        val tearAlpha = (1f - tearDrop).coerceIn(0f, 1f)
        drawCircle(
            color = Color(0xFF93C5FD).copy(alpha = tearAlpha),
            radius = radius * 0.08f,
            center = Offset(rightEyeX + eyeRadius * 0.6f, tearY)
        )

        // Band-aid on left cheek
        drawBandAid(leftEyeX - radius * 0.25f, cy + radius * 0.25f, radius * 0.25f)
    }
}

private fun DrawScope.drawSadMouth(cx: Float, cy: Float, radius: Float) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(cx - radius, cy + radius * 0.3f)
        quadraticTo(cx, cy - radius * 0.4f, cx + radius, cy + radius * 0.3f)
    }
    drawPath(
        path = path,
        color = Color(0xFF0F172A),
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = 4f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    )
}

private fun DrawScope.drawBandAid(x: Float, y: Float, size: Float) {
    val rect = androidx.compose.ui.geometry.Rect(
        offset = Offset(x - size, y - size * 0.4f),
        size = Size(size * 2f, size * 0.8f)
    )
    drawRoundRect(
        color = Color(0xFFFDE68A),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    // Small cross stitch
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 2.5f,
        center = Offset(x, y)
    )
}
