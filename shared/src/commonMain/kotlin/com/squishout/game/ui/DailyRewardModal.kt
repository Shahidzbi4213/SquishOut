package com.squishout.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.data.repository.DAILY_REWARDS_SCHEDULE
import com.squishout.game.data.repository.DailyReward
import com.squishout.game.theme.AmberGlow
import com.squishout.game.theme.EmeraldMint
import com.squishout.game.theme.FrostedWhite
import com.squishout.game.theme.SlateCharcoal

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
        enter = fadeIn(tween(200)) + scaleIn(tween(250, easing = FastOutSlowInEasing), initialScale = 0.9f),
        exit = fadeOut(tween(180)) + scaleOut(tween(200), targetScale = 0.9f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = FrostedWhite),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 440.dp)
                    .shadow(28.dp, RoundedCornerShape(32.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar with Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🗓️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAILY REWARDS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = SlateCharcoal
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✕", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateCharcoal)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Claim consecutive daily rewards to unlock the Day 7 Mega Box!",
                        fontSize = 12.sp,
                        color = SlateCharcoal.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Row 1: Days 1 to 4
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DAILY_REWARDS_SCHEDULE.take(4).forEach { reward ->
                            RewardDayCard(
                                reward = reward,
                                isCompleted = reward.day < currentStreakDay || (reward.day == currentStreakDay && !canClaimToday),
                                isCurrent = reward.day == currentStreakDay && canClaimToday,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Days 5 to 6
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DAILY_REWARDS_SCHEDULE.subList(4, 6).forEach { reward ->
                            RewardDayCard(
                                reward = reward,
                                isCompleted = reward.day < currentStreakDay || (reward.day == currentStreakDay && !canClaimToday),
                                isCurrent = reward.day == currentStreakDay && canClaimToday,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Day 7 Mega Card
                        val day7 = DAILY_REWARDS_SCHEDULE[6]
                        val isDay7Completed = day7.day < currentStreakDay || (day7.day == currentStreakDay && !canClaimToday)
                        val isDay7Current = day7.day == currentStreakDay && canClaimToday

                        RewardDayCard(
                            reward = day7,
                            isCompleted = isDay7Completed,
                            isCurrent = isDay7Current,
                            isMega = true,
                            modifier = Modifier.weight(2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // CTA Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(if (canClaimToday) 6.dp else 0.dp, RoundedCornerShape(26.dp), spotColor = EmeraldMint)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                if (canClaimToday) {
                                    Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                                }
                            )
                            .clickable(enabled = canClaimToday) { onClaimReward() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (canClaimToday) "🎁 CLAIM DAY $currentStreakDay REWARD" else "✓ COME BACK TOMORROW",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardDayCard(
    reward: DailyReward,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isMega: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isCurrent -> EmeraldMint
        isCompleted -> Color(0xFFCBD5E1)
        else -> Color.Transparent
    }

    val cardBg = when {
        isCurrent -> Color(0xFFECFDF5)
        isMega -> Color(0xFFFEF3C7)
        isCompleted -> Color(0xFFF1F5F9)
        else -> Color(0xFFF8FAFC)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(if (isCurrent) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "DAY ${reward.day}",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCurrent) EmeraldMint else SlateCharcoal.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isCompleted) "✓" else reward.icon,
                fontSize = if (isCompleted) 20.sp else 24.sp,
                color = if (isCompleted) EmeraldMint else Color.Unspecified
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = reward.rewardTitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SlateCharcoal,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
