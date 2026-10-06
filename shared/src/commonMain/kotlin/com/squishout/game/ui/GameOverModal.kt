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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.theme.BlueberryBase
import com.squishout.game.theme.BlueberryGloss
import com.squishout.game.theme.CoralHeart
import com.squishout.game.theme.SlateCharcoal
import com.squishout.game.ui.components.CandyHeartsView
import com.squishout.game.ui.components.ModalButtonVariant
import com.squishout.game.ui.components.ModalExtrudedButton
import com.squishout.game.ui.components.ModalPlayIcon
import com.squishout.game.ui.components.ModalRestartIcon

/**
 * Casual Game 3D Game Over / Out of Hearts Modal.
 * Rebuilt as a glazed ceramic porcelain plaque with:
 * - Empathy-driven sad blue mascot with tear and band-aid animations
 * - 3D candy empty hearts tray
 * - Tactile 3D extruded push buttons for Free Rewarded Ad Continue and Gem Revive
 * - Corner brass rivets and ambient dark backdrop scrim
 */
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
        enter = fadeIn(tween(220)) + scaleIn(tween(260, easing = FastOutSlowInEasing), initialScale = 0.88f),
        exit = fadeOut(tween(180)) + scaleOut(tween(200), targetScale = 0.90f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Ambient Dark Backdrop Scrim (spans 100% full bleed edge-to-edge)
                .background(Color(0xFF1E0E14).copy(alpha = 0.70f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Coral Glow Halo
            Box(
                modifier = Modifier
                    .size(360.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF43F5E).copy(alpha = 0.16f),
                                Color(0xFFFB7185).copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Porcelain Plaque Container
            val plaqueShape = RoundedCornerShape(32.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .widthIn(max = 400.dp)
                    .shadow(18.dp, plaqueShape, spotColor = Color(0xFF4A1020))
                    .clip(plaqueShape)
                    // 3D bottom wood/caramel rim bevel
                    .background(Color(0xFF8D4F0E))
                    .padding(bottom = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                // Plaque Face with glazed cream porcelain gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 27.dp, bottomEnd = 27.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFFDF8),
                                    Color(0xFFFBF4E8),
                                    Color(0xFFF5E8D6)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = Color(0xFFFFE8D1),
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 27.dp, bottomEnd = 27.dp)
                        )
                        .padding(horizontal = 22.dp, vertical = 26.dp)
                ) {
                    // Corner Brass Rivets
                    Box(modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) { GameOverBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) { GameOverBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)) { GameOverBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)) { GameOverBrassRivet() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 1. Sad Droopy Mascot Blob with animated tear
                        SadMascotBlob(modifier = Modifier.size(105.dp))

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Headline with Warm 3D Relief Shadow
                        Text(
                            text = "OUT OF HEARTS!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = CoralHeart,
                            letterSpacing = 0.5.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0x33FB7185),
                                    offset = Offset(0f, 3f),
                                    blurRadius = 2f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "The jellies got squished!\nDon't leave them trapped in the tray!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateCharcoal.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Candy Empty Hearts Tray
                        CandyHeartsView(hearts = 0, maxHearts = 3)

                        Spacer(modifier = Modifier.height(20.dp))

                        // 4. Action Buttons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Primary Ad Revive (+3 Hearts)
                            ModalExtrudedButton(
                                text = "CONTINUE (+3 ❤️)",
                                variant = ModalButtonVariant.EMERALD,
                                onClick = onReviveWithAd,
                                leadingIcon = { ModalPlayIcon() }
                            )

                            // Gem Revive (10 Gems)
                            ModalExtrudedButton(
                                text = "REVIVE (10 GEMS)",
                                variant = ModalButtonVariant.CREAM,
                                onClick = onReviveWithGems,
                                leadingIcon = { GemFacetedIcon() }
                            )

                            // Give Up & Restart
                            ModalExtrudedButton(
                                text = "Give Up & Restart",
                                variant = ModalButtonVariant.CORAL,
                                onClick = onRestart,
                                leadingIcon = { ModalRestartIcon(color = Color(0xFFB91C1C)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SadMascotBlob(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "TearTransition")
    val tearDrop by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TearDrop"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width * 0.40f

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
                radius = radius * 1.25f
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
    val path = Path().apply {
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
    drawCircle(
        color = Color(0xFFF59E0B),
        radius = 2.5f,
        center = Offset(x, y)
    )
}

@Composable
private fun GemFacetedIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        val w = size.width
        val h = size.height

        val gemPath = Path().apply {
            moveTo(w * 0.25f, h * 0.15f)
            lineTo(w * 0.75f, h * 0.15f)
            lineTo(w * 0.95f, h * 0.45f)
            lineTo(w * 0.50f, h * 0.92f)
            lineTo(w * 0.05f, h * 0.45f)
            close()
        }
        drawPath(
            path = gemPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF67E8F9), Color(0xFF06B6D4), Color(0xFF0891B2))
            ),
            style = Fill
        )
    }
}

@Composable
private fun GameOverBrassRivet() {
    Canvas(modifier = Modifier.size(8.dp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = Color(0xFF6D3804), radius = radius, center = center)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F), Color(0xFFD97706)),
                center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                radius = radius * 0.9f
            ),
            radius = radius * 0.82f,
            center = center
        )
    }
}
