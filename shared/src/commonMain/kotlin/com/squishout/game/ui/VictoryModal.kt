package com.squishout.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.theme.SquishColors
import com.squishout.game.theme.SquishTypography

@Composable
fun VictoryModal(
    stars: Int,
    score: Int,
    stage: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        // Confetti Particle FX
        ConfettiBurstView(modifier = Modifier.fillMaxSize())

        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .widthIn(max = 420.dp)
                .shadow(24.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3 Golden Stars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val isLit = i <= stars
                        val starColor = if (isLit) SquishColors.StarGold else Color(0xFFE2E8F0)
                        val starSize = if (i == 2) 48.sp else 38.sp
                        Text(
                            text = "★",
                            fontSize = starSize,
                            color = starColor,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = "STAGE CLEARED!",
                    style = SquishTypography.headlineMedium,
                    color = SquishColors.Strawberry,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Flawless Sweet Run! 🍬",
                    style = SquishTypography.bodyMedium,
                    color = SquishColors.KiwiDark
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Score card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SquishColors.BackgroundMint)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Score", style = SquishTypography.titleMedium)
                        Text(
                            text = "$score pts",
                            style = SquishTypography.titleLarge,
                            color = SquishColors.TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Next Level CTA
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SquishColors.Kiwi)
                ) {
                    Text(
                        text = "NEXT LEVEL →",
                        style = SquishTypography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Replay Level
                TextButton(onClick = onReplay) {
                    Text(
                        text = "↺ Replay Stage $stage",
                        style = SquishTypography.bodyMedium,
                        color = SquishColors.TextSecondary
                    )
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val relX: Float,
    val initialYOffset: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    val color: Color,
    val isCircle: Boolean
)

@Composable
private fun ConfettiBurstView(modifier: Modifier = Modifier) {
    val progress = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 2200,
                easing = androidx.compose.animation.core.LinearEasing
            )
        )
    }

    val particles = androidx.compose.runtime.remember {
        val colors = listOf(
            SquishColors.Strawberry,
            SquishColors.StrawberryLight,
            SquishColors.Blueberry,
            SquishColors.Lemon,
            SquishColors.Kiwi,
            SquishColors.Grape,
            SquishColors.StarGold
        )
        (0..45).map {
            ConfettiParticle(
                relX = kotlin.random.Random.nextFloat(),
                initialYOffset = kotlin.random.Random.nextFloat() * 120f,
                speedY = 400f + kotlin.random.Random.nextFloat() * 500f,
                speedX = (kotlin.random.Random.nextFloat() - 0.5f) * 150f,
                size = 8f + kotlin.random.Random.nextFloat() * 10f,
                color = colors[it % colors.size],
                isCircle = it % 2 == 0
            )
        }
    }

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val p = progress.value
        val screenW = size.width
        val screenH = size.height

        for (particle in particles) {
            val currentX = (particle.relX * screenW) + (particle.speedX * p)
            val currentY = -particle.initialYOffset + (particle.speedY * p * (screenH / 600f))
            val alpha = (1f - (p * 0.9f)).coerceIn(0f, 1f)

            if (currentY in 0f..screenH) {
                if (particle.isCircle) {
                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size * 0.5f,
                        center = androidx.compose.ui.geometry.Offset(currentX, currentY)
                    )
                } else {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = androidx.compose.ui.geometry.Offset(currentX, currentY),
                        size = androidx.compose.ui.geometry.Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}
