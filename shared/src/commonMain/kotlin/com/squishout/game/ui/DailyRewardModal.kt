package com.squishout.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.data.repository.DAILY_REWARDS_SCHEDULE
import com.squishout.game.data.repository.DailyReward
import com.squishout.game.theme.AmberGlow
import com.squishout.game.theme.EmeraldMint
import com.squishout.game.theme.SlateCharcoal
import com.squishout.game.ui.components.ModalButtonVariant
import com.squishout.game.ui.components.ModalExtrudedButton

/**
 * Casual Game 3D Daily Rewards Calendar Modal.
 * Redesigned into a glazed porcelain plaque with:
 * - 3D Gift Box header with celebratory sparkles
 * - Tactile 7-day calendar streak cards with golden glow highlights
 * - Mega Box highlight card on Day 7
 * - 3D tactile extruded push buttons for claiming rewards
 */
@Composable
fun DailyRewardModal(
    isVisible: Boolean,
    currentStreakDay: Int,
    canClaimToday: Boolean,
    onClaimReward: () -> Unit,
    onDismiss: () -> Unit,
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
                .background(Color(0xFF0D1F14).copy(alpha = 0.70f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Radial Golden Halo
            Box(
                modifier = Modifier
                    .size(380.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD54F).copy(alpha = 0.18f),
                                Color(0xFF10B981).copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Porcelain Plaque Container
            val plaqueShape = RoundedCornerShape(32.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 440.dp)
                    .shadow(20.dp, plaqueShape, spotColor = Color(0xFF6D3804))
                    .clip(plaqueShape)
                    .background(Color(0xFF8D4F0E))
                    .padding(bottom = 5.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                contentAlignment = Alignment.Center
            ) {
                // Plaque Face with glazed porcelain cream gradient
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
                        .padding(horizontal = 18.dp, vertical = 22.dp)
                ) {
                    // Corner Brass Rivets
                    Box(modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) { DailyRewardBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) { DailyRewardBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)) { DailyRewardBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)) { DailyRewardBrassRivet() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Top Header: Close Button and Title
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Close Button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                                    .shadow(2.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE3D2))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateCharcoal
                                )
                            }

                            // Centered Title & Mascot Icon
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                DailyGiftBoxIcon(modifier = Modifier.size(52.dp))

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "DAILY STREAK!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SlateCharcoal,
                                    letterSpacing = 0.6.sp,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color(0x33D6B588),
                                            offset = Offset(0f, 2f),
                                            blurRadius = 2f
                                        )
                                    )
                                )

                                Text(
                                    text = "Check in every day to claim the Day 7 Mega Box!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF8A7E6E),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Days 1–6 in a 3x2 Grid
                        val days1to6 = DAILY_REWARDS_SCHEDULE.take(6)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (row in 0..1) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (col in 0..2) {
                                        val index = row * 3 + col
                                        val reward = days1to6[index]
                                        val dayNum = reward.day
                                        val isClaimed = dayNum < currentStreakDay || (dayNum == currentStreakDay && !canClaimToday)
                                        val isToday = dayNum == currentStreakDay && canClaimToday

                                        DailyRewardDayCard(
                                            reward = reward,
                                            isClaimed = isClaimed,
                                            isToday = isToday,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day 7 Mega Box Card
                        val day7Reward = DAILY_REWARDS_SCHEDULE.last()
                        val isDay7Claimed = 7 < currentStreakDay || (7 == currentStreakDay && !canClaimToday)
                        val isDay7Today = 7 == currentStreakDay && canClaimToday
                        DailyMegaBoxCard(
                            reward = day7Reward,
                            isClaimed = isDay7Claimed,
                            isToday = isDay7Today,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Claim Action Button
                        if (canClaimToday) {
                            ModalExtrudedButton(
                                text = "CLAIM DAY $currentStreakDay REWARD",
                                variant = ModalButtonVariant.EMERALD,
                                onClick = onClaimReward,
                                leadingIcon = { DailyGiftSparkleIcon() }
                            )
                        } else {
                            ModalExtrudedButton(
                                text = "COME BACK TOMORROW!",
                                variant = ModalButtonVariant.CREAM,
                                onClick = onDismiss
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyRewardDayCard(
    reward: DailyReward,
    isClaimed: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .height(82.dp)
            .shadow(if (isToday) 4.dp else 1.dp, cardShape, spotColor = if (isToday) AmberGlow else Color.Black.copy(alpha = 0.1f))
            .clip(cardShape)
            .background(
                when {
                    isToday -> Color(0xFFFEF9C3) // Golden highlight
                    isClaimed -> Color(0xFFE2E8F0) // Muted past
                    else -> Color(0xFFEDE3D2)    // Warm cream well
                }
            )
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = when {
                    isToday -> Color(0xFFF59E0B)
                    isClaimed -> Color(0xFFCBD5E1)
                    else -> Color(0xFFE2D4BF)
                },
                shape = cardShape
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "DAY ${reward.day}",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isToday) Color(0xFFB45309) else Color(0xFF8A7E6E)
            )

            // Reward Visual Icon
            when {
                isClaimed -> {
                    Text(text = "✓", fontSize = 18.sp, fontWeight = FontWeight.Black, color = EmeraldMint)
                }
                reward.diamonds > 0 -> {
                    Text(text = "💎", fontSize = 18.sp)
                }
                reward.lives > 0 -> {
                    Text(text = "❤️", fontSize = 18.sp)
                }
            }

            Text(
                text = when {
                    isClaimed -> "Claimed"
                    reward.diamonds > 0 -> "+${reward.diamonds}"
                    reward.lives > 0 -> "+${reward.lives}"
                    else -> ""
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isClaimed) Color(0xFF059669) else SlateCharcoal
            )
        }
    }
}

@Composable
private fun DailyMegaBoxCard(
    reward: DailyReward,
    isClaimed: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .height(56.dp)
            .shadow(if (isToday) 6.dp else 2.dp, cardShape, spotColor = AmberGlow)
            .clip(cardShape)
            .background(
                Brush.horizontalGradient(
                    colors = if (isClaimed) {
                        listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                    } else {
                        listOf(Color(0xFFFEF08A), Color(0xFFFDE047), Color(0xFFFBBF24))
                    }
                )
            )
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = if (isToday) Color(0xFFD97706) else Color(0xFFF59E0B).copy(alpha = 0.6f),
                shape = cardShape
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎁", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "DAY 7 MEGA BOX",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isClaimed) Color(0xFF64748B) else Color(0xFF78350F)
                    )
                    Text(
                        text = "+150 💎 + All Boosters!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClaimed) Color(0xFF94A3B8) else Color(0xFF92400E)
                    )
                }
            }

            if (isClaimed) {
                Text(text = "✓ Claimed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
            } else if (isToday) {
                Text(text = "READY! ★", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFB45309))
            }
        }
    }
}

@Composable
private fun DailyGiftBoxIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Gift Box Base
        val boxRect = androidx.compose.ui.geometry.RoundRect(
            rect = androidx.compose.ui.geometry.Rect(w * 0.18f, h * 0.35f, w * 0.82f, h * 0.90f),
            radiusX = 8.dp.toPx(),
            radiusY = 8.dp.toPx()
        )
        val boxPath = Path().apply { addRoundRect(boxRect) }
        drawPath(
            path = boxPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFB7185), Color(0xFFE11D48), Color(0xFFBE123C))
            ),
            style = Fill
        )

        // Golden Ribbon Vertical
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFFFE082), Color(0xFFFFD54F), Color(0xFFF59E0B))
            ),
            topLeft = Offset(w * 0.44f, h * 0.35f),
            size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.55f)
        )

        // Box Lid
        val lidRect = androidx.compose.ui.geometry.RoundRect(
            rect = androidx.compose.ui.geometry.Rect(w * 0.14f, h * 0.25f, w * 0.86f, h * 0.40f),
            radiusX = 6.dp.toPx(),
            radiusY = 6.dp.toPx()
        )
        val lidPath = Path().apply { addRoundRect(lidRect) }
        drawPath(
            path = lidPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFF85A1), Color(0xFFFB7185))
            ),
            style = Fill
        )

        // Ribbon Bow on Top
        drawCircle(color = Color(0xFFFFD54F), radius = w * 0.08f, center = Offset(w * 0.40f, h * 0.20f))
        drawCircle(color = Color(0xFFFFD54F), radius = w * 0.08f, center = Offset(w * 0.60f, h * 0.20f))
        drawCircle(color = Color(0xFFF59E0B), radius = w * 0.05f, center = Offset(w * 0.50f, h * 0.22f))
    }
}

@Composable
private fun DailyGiftSparkleIcon() {
    Canvas(modifier = Modifier.size(16.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension * 0.45f

        val path = Path().apply {
            moveTo(center.x, center.y - r)
            quadraticTo(center.x, center.y, center.x + r, center.y)
            quadraticTo(center.x, center.y, center.x, center.y + r)
            quadraticTo(center.x, center.y, center.x - r, center.y)
            quadraticTo(center.x, center.y, center.x, center.y - r)
            close()
        }
        drawPath(path = path, color = Color.White, style = Fill)
    }
}

@Composable
private fun DailyRewardBrassRivet() {
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
