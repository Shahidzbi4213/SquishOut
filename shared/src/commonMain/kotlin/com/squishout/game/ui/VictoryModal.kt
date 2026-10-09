package com.squishout.game.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.squishout.game.theme.SquishColors
import kotlin.random.Random

/**
 * Casual Game 3D Level Victory / Stage Cleared Modal.
 * Faithfully matches Stitch's juicy toy arcade design with:
 * - Ambient backdrop scrim & glowing radial blur
 * - Floating confetti & sparkles overlay
 * - Top floating 3-stars arch (tilted left/right, elevated hero center with pulsing gold halo)
 * - Celebratory mascot hero (bouncing Strawberry Blob with crown & MVP badge)
 * - Candy headline ("STAGE CLEARED!") with 3D text relief shadow
 * - Recessed soft-cream capsule for Score and Jelly Bonus (+300 🍬)
 * - 3D tactile extruded push button for "NEXT LEVEL"
 * - Replay Level ghost link and bottom encouragement pill
 */
@Composable
fun VictoryModal(
    stars: Int,
    score: Int,
    stage: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bonusDiamonds = 10 + (stars * 5)

    Box(
        modifier = modifier
            .fillMaxSize()
            // Ambient Backdrop Scrim (Level 4 suspension)
            .background(Color(0xFF0D1F14).copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = true
            ) { /* Consume taps outside modal */ },
        contentAlignment = Alignment.Center
    ) {
        // Glowing Ambient Radial Halo in Center
        Box(
            modifier = Modifier
                .size(340.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFEE440).copy(alpha = 0.25f),
                            Color(0xFF54FDC4).copy(alpha = 0.15f),
                            Color(0xFFFFCCD5).copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Floating Confetti & Sparkles Particle Overlay
        ConfettiBurstView(modifier = Modifier.fillMaxSize())

        // Modal Stage Architecture
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(horizontal = 16.dp)
        ) {
            // Stack: Top Floating Stars Arch + Victory Card
            Box(
                contentAlignment = Alignment.TopCenter
            ) {
                // VICTORY MODAL CARD CONTAINER (320px baseline)
                Box(
                    modifier = Modifier
                        .padding(top = 40.dp) // Generous clearance for star arch to crown top edge
                        .width(320.dp)
                        .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFFC83250).copy(alpha = 0.30f))
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFFE2F7E8).copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Celebratory Mascot Hero (Strawberry Blobby with MVP Badge)
                        CelebratoryMascotHero(
                            modifier = Modifier.size(96.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Candy Headline & Subtext
                        Text(
                            text = "STAGE CLEARED!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF4D6D),
                            letterSpacing = (-0.5).sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFFC9184A),
                                    offset = Offset(0f, 2.5f),
                                    blurRadius = 0f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "🌱", fontSize = 13.sp)
                            Text(
                                text = "Flawless Sweet Run!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF059669)
                            )
                            Text(text = "🌱", fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Score & Diamond Reward Capsule (Recessed Soft Cream Card)
                        ScoreAndBonusCapsule(
                            score = score,
                            bonusDiamonds = bonusDiamonds
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action Buttons Cluster
                        // Primary 3D Push Button: NEXT LEVEL
                        JuicyNextLevelButton(
                            onClick = onNextLevel
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Secondary Replay Ghost Link
                        ReplayGhostButton(
                            stage = stage,
                            onClick = onReplay
                        )
                    }
                }

                // TOP FLOATING STARS ARCH (Overlapping top edge of modal card)
                TopFloatingStarsArch(
                    stars = stars,
                    modifier = Modifier.zIndex(4f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Whimsical Encouragement Pill Footer
            StageCompletedFooterPill(stage = stage)
        }
    }
}

/* =========================================================================
   TOP FLOATING STARS ARCH
   ========================================================================= */

@Composable
private fun TopFloatingStarsArch(
    stars: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // STAR 1 (Left): -12° tilt
        ArcadeStar(
            isLit = stars >= 1,
            sizeDp = 44,
            rotation = -12f,
            showHalo = false
        )

        // STAR 2 (Hero Center): Elevated, 62dp, pulsing gold halo
        ArcadeStar(
            isLit = stars >= 2,
            sizeDp = 62,
            rotation = 0f,
            showHalo = stars >= 2,
            modifier = Modifier.offset(y = (-10).dp)
        )

        // STAR 3 (Right): +12° tilt
        ArcadeStar(
            isLit = stars >= 3,
            sizeDp = 44,
            rotation = 12f,
            showHalo = false
        )
    }
}

@Composable
private fun ArcadeStar(
    isLit: Boolean,
    sizeDp: Int,
    rotation: Float,
    showHalo: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .rotate(rotation)
            .size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing Gold Halo (for lit center hero star)
        if (showHalo) {
            Box(
                modifier = Modifier
                    .size((sizeDp * 1.25f).dp)
                    .scale(haloPulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFEE440).copy(alpha = 0.45f),
                                Color(0xFFFFB703).copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // 3D Star Canvas Drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val starPath = createSvgStarPath(w, h)

            // 1. Ambient / 3D drop shadow
            val shadowOffset = if (isLit) 3.5f else 2f
            val shadowPath = createSvgStarPath(w, h, offsetY = shadowOffset)
            drawPath(
                path = shadowPath,
                color = if (isLit) Color(0xFFC2410C).copy(alpha = 0.45f) else Color(0xFF64748B).copy(alpha = 0.25f)
            )

            // 2. Star Gradient Fill
            val fillBrush = if (isLit) {
                if (sizeDp > 50) {
                    // Center Hero Star: Radiant deep gold gradient
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFDE7),
                            Color(0xFFFEE440),
                            Color(0xFFFFB703),
                            Color(0xFFFB8500)
                        )
                    )
                } else {
                    // Side Stars: Vibrant gold gradient
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF176),
                            Color(0xFFFFD166),
                            Color(0xFFFF9E00)
                        )
                    )
                }
            } else {
                // Unlit grey/cream star
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFE2E8F0),
                        Color(0xFFCBD5E1)
                    )
                )
            }

            drawPath(
                path = starPath,
                brush = fillBrush
            )

            // 3. Specular gloss highlight on top star tip
            if (isLit) {
                drawOval(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.35f, h * 0.12f),
                    size = Size(w * 0.30f, h * 0.16f)
                )
            }
        }

        // Micro Sparkle Accent
        if (isLit) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
            ) {
                Text(text = "✨", fontSize = (sizeDp * 0.28f).sp)
            }
        }
    }
}

/**
 * Normalized 5-pointed star matching Stitch SVG path:
 * M12 2L14.9 8.26L21.8 9.27L16.8 14.14L18 21.02L12 17.77L6 21.02L7.2 14.14L2.2 9.27L9.1 8.26L12 2Z
 */
private fun createSvgStarPath(width: Float, height: Float, offsetX: Float = 0f, offsetY: Float = 0f): Path {
    val sx = width / 24f
    val sy = height / 24f
    return Path().apply {
        moveTo(12f * sx + offsetX, 2f * sy + offsetY)
        lineTo(14.9f * sx + offsetX, 8.26f * sy + offsetY)
        lineTo(21.8f * sx + offsetX, 9.27f * sy + offsetY)
        lineTo(16.8f * sx + offsetX, 14.14f * sy + offsetY)
        lineTo(18f * sx + offsetX, 21.02f * sy + offsetY)
        lineTo(12f * sx + offsetX, 17.77f * sy + offsetY)
        lineTo(6f * sx + offsetX, 21.02f * sy + offsetY)
        lineTo(7.2f * sx + offsetX, 14.14f * sy + offsetY)
        lineTo(2.2f * sx + offsetX, 9.27f * sy + offsetY)
        lineTo(9.1f * sx + offsetX, 8.26f * sy + offsetY)
        close()
    }
}

/* =========================================================================
   CELEBRATORY MASCOT HERO (Strawberry Blob with Crown & MVP Badge)
   ========================================================================= */

@Composable
private fun CelebratoryMascotHero(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (-6f),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Soft diffused ground drop shadow
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp)
                .size(72.dp, 12.dp)
        ) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFC9184A).copy(alpha = 0.22f),
                        Color(0xFFC9184A).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width / 2f
                )
            )
        }

        // Mascot Disc with bouncing motion
        Box(
            modifier = Modifier
                .offset(y = floatY.dp)
                .size(86.dp)
                .shadow(8.dp, CircleShape, spotColor = Color(0xFFC9184A).copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFCCD5), Color(0xFFFFA8BA))
                    )
                )
                .border(2.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Kawaii Strawberry Jelly Character Canvas
            Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.width * 0.44f

                // Body Gummy Fill
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF758F),
                            Color(0xFFFF4D6D),
                            Color(0xFFD90429)
                        ),
                        center = Offset(cx - radius * 0.25f, cy - radius * 0.25f),
                        radius = radius * 1.2f
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )

                // Top Specular Highlight Crescent
                drawOval(
                    color = Color.White.copy(alpha = 0.75f),
                    topLeft = Offset(cx - radius * 0.55f, cy - radius * 0.72f),
                    size = Size(radius * 0.75f, radius * 0.34f)
                )

                // Pink Blush Cheeks
                drawCircle(
                    color = Color(0xFFFFB3C1).copy(alpha = 0.85f),
                    radius = radius * 0.16f,
                    center = Offset(cx - radius * 0.50f, cy + radius * 0.12f)
                )
                drawCircle(
                    color = Color(0xFFFFB3C1).copy(alpha = 0.85f),
                    radius = radius * 0.16f,
                    center = Offset(cx + radius * 0.50f, cy + radius * 0.12f)
                )

                // Kawaii Eyes
                val eyeRadius = radius * 0.15f
                val eyeY = cy - radius * 0.04f
                val leftEyeX = cx - radius * 0.32f
                val rightEyeX = cx + radius * 0.32f

                drawCircle(color = Color(0xFF0F172A), radius = eyeRadius, center = Offset(leftEyeX, eyeY))
                drawCircle(color = Color(0xFF0F172A), radius = eyeRadius, center = Offset(rightEyeX, eyeY))

                drawCircle(color = Color.White, radius = eyeRadius * 0.48f, center = Offset(leftEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))
                drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(leftEyeX + eyeRadius * 0.30f, eyeY + eyeRadius * 0.25f))

                drawCircle(color = Color.White, radius = eyeRadius * 0.48f, center = Offset(rightEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))
                drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(rightEyeX + eyeRadius * 0.30f, eyeY + eyeRadius * 0.25f))

                // Cheerful Smile
                val mouthPath = Path().apply {
                    moveTo(cx - radius * 0.18f, cy + radius * 0.18f)
                    quadraticTo(cx, cy + radius * 0.40f, cx + radius * 0.18f, cy + radius * 0.18f)
                }
                drawPath(
                    path = mouthPath,
                    color = Color(0xFF0F172A),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            }

            // Crown ornament
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
            ) {
                Text(text = "👑", fontSize = 15.sp)
            }
        }

        // MVP Ribbon Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-2).dp)
                .shadow(3.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFF4B8), Color(0xFFFEE440), Color(0xFFF59E0B))
                    )
                )
                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 6.dp, vertical = 1.5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = "★", fontSize = 8.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Black)
                Text(text = "MVP", fontSize = 9.sp, color = Color(0xFF78350F), fontWeight = FontWeight.Black)
            }
        }
    }
}

/* =========================================================================
   SCORE & DIAMOND REWARD CAPSULE (Recessed Soft Cream Card)
   ========================================================================= */

@Composable
private fun ScoreAndBonusCapsule(
    score: Int,
    bonusDiamonds: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFDFBF7))
            .border(1.dp, Color(0xFFFED7AA).copy(alpha = 0.50f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ROW 1: Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🏆", fontSize = 16.sp)
                    Text(
                        text = "Score",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6C757D)
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = formatScoreNumber(score),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0D1F14)
                    )
                    Text(
                        text = "pts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6C757D),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFFED7AA).copy(alpha = 0.40f))
            )

            // ROW 2: Diamond Reward
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "💎", fontSize = 16.sp)
                    Text(
                        text = "Diamond Reward",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6C757D)
                    )
                }

                // Raised Diamond Pill Badge
                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFBAE6FD).copy(alpha = 0.60f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "+$bonusDiamonds",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0284C7)
                        )
                        Text(
                            text = "💎",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatScoreNumber(score: Int): String {
    val s = score.toString()
    val len = s.length
    if (len <= 3) return s
    val sb = StringBuilder()
    for (i in s.indices) {
        if (i > 0 && (len - i) % 3 == 0) {
            sb.append(',')
        }
        sb.append(s[i])
    }
    return sb.toString()
}

/* =========================================================================
   ACTION BUTTONS: 3D JUICY NEXT LEVEL & REPLAY
   ========================================================================= */

@Composable
private fun JuicyNextLevelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100)
    )

    val buttonShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .shadow(6.dp, buttonShape, spotColor = Color(0xFF059669).copy(alpha = 0.4f))
            .clip(buttonShape)
            // 3D bottom bevel rim (#059669)
            .background(Color(0xFF059669))
            .padding(bottom = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF06D6A0), // Emerald mint
                            Color(0xFF059669)  // Deep rich emerald
                        )
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Top Lip Specular Pill Highlight
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 3.dp)
                    .fillMaxWidth(0.92f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f))
            )

            // Button Label & Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NEXT LEVEL",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF003828),
                            offset = Offset(0f, 1.5f),
                            blurRadius = 1f
                        )
                    )
                )
                Text(
                    text = "➔",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ReplayGhostButton(
    stage: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "↺", fontSize = 15.sp, color = Color(0xFF6C757D), fontWeight = FontWeight.Bold)
            Text(
                text = "Replay Level",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF6C757D)
            )
        }
    }
}

/* =========================================================================
   STAGE COMPLETED FOOTER PILL
   ========================================================================= */

@Composable
private fun StageCompletedFooterPill(
    stage: Int,
    modifier: Modifier = Modifier
) {
    val worldNum = when {
        stage <= 20 -> 1
        stage <= 40 -> 2
        else -> 3
    }

    Box(
        modifier = modifier
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.88f))
            .border(1.dp, Color(0xFFD2E8D6), CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF06D6A0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Text(
                text = "WORLD $worldNum • LEVEL $stage COMPLETED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3B4A43),
                letterSpacing = 0.6.sp
            )
        }
    }
}

/* =========================================================================
   FLOATING CONFETTI BURST VIEW
   ========================================================================= */

private data class ConfettiParticle(
    val relX: Float,
    val initialYOffset: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    val color: Color,
    val isCircle: Boolean,
    val rotationSpeed: Float
)

@Composable
private fun ConfettiBurstView(modifier: Modifier = Modifier) {
    val progress = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 2600,
                easing = LinearEasing
            )
        )
    }

    val particles = remember {
        val colors = listOf(
            Color(0xFFFF4D6D), // North primary strawberry
            Color(0xFF06D6A0), // South primary mint
            Color(0xFFFFB703), // West primary honeycomb
            Color(0xFF9D4EDD), // Grape primary
            Color(0xFFFEE440), // Buttercup gold
            Color(0xFF00B4D8), // East primary soda blue
            Color(0xFFFFCCD5)  // Petal pink
        )
        (0..55).map {
            ConfettiParticle(
                relX = Random.nextFloat(),
                initialYOffset = Random.nextFloat() * 160f,
                speedY = 320f + Random.nextFloat() * 450f,
                speedX = (Random.nextFloat() - 0.5f) * 160f,
                size = 7f + Random.nextFloat() * 10f,
                color = colors[it % colors.size],
                isCircle = it % 2 == 0,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 360f
            )
        }
    }

    Canvas(modifier = modifier) {
        val p = progress.value
        val screenW = size.width
        val screenH = size.height

        for (particle in particles) {
            val currentX = (particle.relX * screenW) + (particle.speedX * p)
            val currentY = -particle.initialYOffset + (particle.speedY * p * (screenH / 600f))
            val alpha = (1f - (p * 0.75f)).coerceIn(0f, 1f)

            if (currentY in 0f..screenH) {
                if (particle.isCircle) {
                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size * 0.5f,
                        center = Offset(currentX, currentY)
                    )
                } else {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(currentX, currentY),
                        size = Size(particle.size * 1.2f, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}
