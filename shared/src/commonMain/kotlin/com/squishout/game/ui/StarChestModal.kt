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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.data.repository.StarChestMilestone
import com.squishout.game.theme.AmberGlow
import com.squishout.game.theme.EmeraldMint
import com.squishout.game.theme.SlateCharcoal
import com.squishout.game.ui.components.Canvas3DStar
import com.squishout.game.ui.components.CanvasGiftChest
import com.squishout.game.ui.components.ModalButtonVariant
import com.squishout.game.ui.components.ModalExtrudedButton

/**
 * Casual Game 3D Star Chest Milestone Modal.
 * Displays milestone loot breakdown with 3D porcelain plaque styling:
 * - 3D Gift Box header with celebratory star aura
 * - Stars collected progress status
 * - Diamonds and Booster reward capsules
 * - Tactile 3D extruded claim button
 */
@Composable
fun StarChestModal(
    milestone: StarChestMilestone?,
    totalStars: Int,
    isClaimed: Boolean,
    onClaim: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = milestone != null,
        enter = fadeIn(tween(220)) + scaleIn(tween(260, easing = FastOutSlowInEasing), initialScale = 0.88f),
        exit = fadeOut(tween(180)) + scaleOut(tween(200), targetScale = 0.90f),
        modifier = modifier
    ) {
        if (milestone == null) return@AnimatedVisibility

        val canClaim = totalStars >= milestone.requiredStars && !isClaimed

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1E16).copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Radial Glow Halo
            Box(
                modifier = Modifier
                    .size(360.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                AmberGlow.copy(alpha = 0.28f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 3D Glazed Ceramic Porcelain Plaque
            val plaqueShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .widthIn(max = 350.dp)
                    .padding(horizontal = 20.dp)
                    .shadow(28.dp, plaqueShape, spotColor = Color(0xFF1E281F).copy(alpha = 0.55f))
                    .clip(plaqueShape)
                    .background(Color(0xFF8D531B)) // 3D caramel bottom bevel lip
                    .padding(bottom = 5.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* absorb clicks inside plaque */ }
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFFFDF8),
                                    Color(0xFFFAF3E3)
                                )
                            )
                        )
                        .border(2.5.dp, Color(0xFFFFECC4), RoundedCornerShape(30.dp))
                        .padding(horizontal = 22.dp, vertical = 24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 1. Star Chest Header Artwork
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(8.dp, CircleShape, spotColor = AmberGlow.copy(alpha = 0.4f))
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFFFF4D0),
                                            Color(0xFFFFDF88)
                                        )
                                    )
                                )
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CanvasGiftChest(
                                canClaim = canClaim,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Headline & Milestone Badge
                        Text(
                            text = "STAR MILESTONE CHEST",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = SlateCharcoal,
                            letterSpacing = 0.4.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Canvas3DStar(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${milestone.requiredStars} Stars Milestone",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD97706)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Pill
                        val progressText = when {
                            isClaimed -> "Milestone Completed & Claimed! ✓"
                            totalStars >= milestone.requiredStars -> "Ready to open! ($totalStars / ${milestone.requiredStars} ⭐)"
                            else -> "Progress: $totalStars / ${milestone.requiredStars} ⭐ (Need ${milestone.requiredStars - totalStars} more)"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3EAD3))
                                .padding(vertical = 6.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = progressText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isClaimed) Color(0xFF059669) else Color(0xFF78572A),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Rewards Capsule Breakdown
                        Text(
                            text = "CHEST CONTAINS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFA19788),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Diamonds Reward Row
                            RewardItemRow(
                                title = "+${milestone.diamonds} Diamonds",
                                iconEmoji = "💎",
                                accentColor = Color(0xFF0284C7)
                            )

                            // Boosters if any
                            if (milestone.hintBoosters > 0) {
                                RewardItemRow(
                                    title = "+${milestone.hintBoosters} Hint Bulb",
                                    iconEmoji = "💡",
                                    accentColor = Color(0xFFF59E0B)
                                )
                            }
                            if (milestone.undoBoosters > 0) {
                                RewardItemRow(
                                    title = "+${milestone.undoBoosters} Undo Steps",
                                    iconEmoji = "↺",
                                    accentColor = Color(0xFF8B5CF6)
                                )
                            }
                            if (milestone.wandBoosters > 0) {
                                RewardItemRow(
                                    title = "+${milestone.wandBoosters} Squish Wand",
                                    iconEmoji = "🪄",
                                    accentColor = Color(0xFFEC4899)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 4. Action CTA Button
                        when {
                            canClaim -> {
                                ModalExtrudedButton(
                                    text = "🎁  CLAIM REWARD!",
                                    variant = ModalButtonVariant.EMERALD,
                                    onClick = onClaim
                                )
                            }
                            isClaimed -> {
                                ModalExtrudedButton(
                                    text = "ALREADY CLAIMED ✓",
                                    variant = ModalButtonVariant.CREAM,
                                    onClick = onDismiss
                                )
                            }
                            else -> {
                                ModalExtrudedButton(
                                    text = "KEEP PLAYING",
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
}

@Composable
private fun RewardItemRow(
    title: String,
    iconEmoji: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEADBCE), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = iconEmoji,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SlateCharcoal
            )
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
    }
}
