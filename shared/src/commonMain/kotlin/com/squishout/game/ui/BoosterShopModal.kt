package com.squishout.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.theme.AmberGlow
import com.squishout.game.theme.EmeraldMint
import com.squishout.game.theme.FrostedWhite
import com.squishout.game.theme.KiwiBase
import com.squishout.game.theme.LemonBase
import com.squishout.game.theme.SlateCharcoal
import com.squishout.game.theme.StrawberryBase

@Composable
fun BoosterShopModal(
    isVisible: Boolean,
    candies: Int,
    gems: Int,
    onBuyUndo: () -> Unit,
    onBuyHint: () -> Unit,
    onBuyWand: () -> Unit,
    onRefillHeartsGems: () -> Unit,
    onRefillHeartAd: () -> Unit,
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
                    .shadow(28.dp, RoundedCornerShape(32.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {} // Prevent click-through dismiss
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar: Balances & Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Candy and Gem Balances
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CurrencyBadge(icon = "🍬", amount = candies, bgColor = Color(0xFFFCE7F3))
                            CurrencyBadge(icon = "💎", amount = gems, bgColor = Color(0xFFE0E7FF))
                        }

                        // Close "✕" Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateCharcoal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = "BOOSTER SHOP",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = SlateCharcoal,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Power up your game & never get stuck!",
                        fontSize = 12.sp,
                        color = SlateCharcoal.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Item 1: +3 Undos
                    ShopItemCard(
                        icon = "↩",
                        iconBg = KiwiBase,
                        title = "+3 Undos",
                        subtitle = "Rewind moves & restore board",
                        costText = "50 🍬",
                        canAfford = candies >= 50,
                        onBuy = onBuyUndo
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 2: +3 Hints
                    ShopItemCard(
                        icon = "💡",
                        iconBg = StrawberryBase,
                        title = "+3 Hints",
                        subtitle = "Reveal next solvable squishy",
                        costText = "75 🍬",
                        canAfford = candies >= 75,
                        onBuy = onBuyHint
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 3: +1 Magic Wand
                    ShopItemCard(
                        icon = "🪄",
                        iconBg = LemonBase,
                        title = "+1 Magic Wand",
                        subtitle = "Vaporize any asleep blocker",
                        costText = "100 🍬",
                        canAfford = candies >= 100,
                        onBuy = onBuyWand
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 4: Full Heart Refill
                    ShopItemCard(
                        icon = "❤️",
                        iconBg = Color(0xFFF43F5E),
                        title = "Full Life Refill",
                        subtitle = "Refill lives immediately to 5/5",
                        costText = "10 💎",
                        canAfford = gems >= 10,
                        onBuy = onRefillHeartsGems
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Item 5: Free Video Ad Heart
                    ShopItemCard(
                        icon = "🎬",
                        iconBg = AmberGlow,
                        title = "Watch Ad: +1 Life",
                        subtitle = "Free recovery bonus",
                        costText = "FREE",
                        canAfford = true,
                        onBuy = onRefillHeartAd
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    icon: String,
    iconBg: Color,
    title: String,
    subtitle: String,
    costText: String,
    canAfford: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon Bubble
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateCharcoal
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SlateCharcoal.copy(alpha = 0.55f),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Buy Button
        Box(
            modifier = Modifier
                .shadow(if (canAfford) 4.dp else 0.dp, RoundedCornerShape(18.dp), spotColor = EmeraldMint)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (canAfford) {
                        Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                    }
                )
                .clickable(enabled = canAfford) { onBuy() }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = costText,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CurrencyBadge(icon: String, amount: Int, bgColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 13.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$amount",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SlateCharcoal
        )
    }
}
