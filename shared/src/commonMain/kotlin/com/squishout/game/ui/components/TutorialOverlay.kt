package com.squishout.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.theme.SlateCharcoal

/**
 * Sleek, interactive tutorial overlay for Level 1 Onboarding.
 *
 * Explains how Squish Out puzzles work without obstructing player taps on awake jellies.
 * Step 1: Introduces awake vs dozing jellies and exit directions.
 * Step 2: Celebrates the first launch and explains how unblocking wakes up queued jellies.
 */
@Composable
fun TutorialOverlay(
    isStep1: Boolean,
    onGotIt: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    isTargetInTopHalf: Boolean = true
) {
    // Non-blocking outer container: passes all touches outside the card to the board below
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = if (isTargetInTopHalf) Alignment.BottomCenter else Alignment.TopCenter
    ) {
        val verticalPadding = if (isTargetInTopHalf) 72.dp else 78.dp

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { if (isTargetInTopHalf) it / 2 else -it / 2 },
            exit = fadeOut() + slideOutVertically { if (isTargetInTopHalf) it / 2 else -it / 2 }
        ) {
            TutorialCard(
                isStep1 = isStep1,
                onGotIt = onGotIt,
                onSkip = onSkip,
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = verticalPadding)
            )
        }
    }
}

@Composable
private fun TutorialCard(
    isStep1: Boolean,
    onGotIt: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .shadow(10.dp, cardShape)
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFDF8),
                        Color(0xFFFFF8EC),
                        Color(0xFFFEEDDB)
                    )
                )
            )
            .border(2.dp, Color(0xFFF6AD55).copy(alpha = 0.85f), cardShape)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            // Header Bar: Step Pill + Mascot Icon + Tactile Skip Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mascot Emoji Avatar
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(if (isStep1) Color(0xFFFFE4E6) else Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isStep1) "🍓" else "🌟",
                            fontSize = 17.sp
                        )
                    }

                    // Step Tag Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3ECE0))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isStep1) "TUTORIAL • 1 OF 2" else "TUTORIAL • 2 OF 2",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8D5B28),
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                // Tactile Skip Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEE2E2).copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Skip ✕",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Content for Step Transition
            AnimatedContent(
                targetState = isStep1,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TutorialStepContent"
            ) { step1 ->
                Column {
                    if (step1) {
                        // Step 1: Explain the premise
                        Text(
                            text = "Welcome to Squish Out!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = SlateCharcoal,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0x22D6B588),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 2f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Jellies face an exit direction. Awake jellies have an open path to escape. Tap an awake jelly to squish it out!",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Hint Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE8F5E9))
                                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(text = "💡", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Look for eyes open ( ✦‿✦ ) and glowing rim below!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    } else {
                        // Step 2: Explain unblocking and path clearing
                        Text(
                            text = "Nice squish!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = SlateCharcoal,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0x22D6B588),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 2f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Clearing paths wakes up the jellies behind them. Squish all jellies to clear the board!",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ModalExtrudedButton(
                            text = "Got it! 🚀",
                            variant = ModalButtonVariant.EMERALD,
                            onClick = onGotIt,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
