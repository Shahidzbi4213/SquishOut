package com.squishout.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.theme.AmberGlow
import com.squishout.game.theme.CoralHeart
import com.squishout.game.theme.EmeraldMint
import com.squishout.game.theme.KiwiBase
import com.squishout.game.theme.LemonBase
import com.squishout.game.theme.SlateCharcoal
import com.squishout.game.theme.StrawberryBase

/**
 * Casual Game 3D Booster Shop Modal.
 * Redesigned into a glazed porcelain plaque with:
 * - 3D striped awning candy shop header
 * - Currency balance pill badges with custom candy & gem vector icons
 * - Recessed cream well rows with 3D tactile extruded purchase buttons
 * - Tactile press feedback physics and brass screw rivets
 */
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
        enter = fadeIn(tween(220)) + scaleIn(tween(260, easing = FastOutSlowInEasing), initialScale = 0.88f),
        exit = fadeOut(tween(180)) + scaleOut(tween(200), targetScale = 0.90f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Ambient Dark Backdrop Scrim
                .background(Color(0xFF0F172A).copy(alpha = 0.68f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Ambient Warm Radial Halo
            Box(
                modifier = Modifier
                    .size(390.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD54F).copy(alpha = 0.16f),
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
                        .padding(horizontal = 18.dp, vertical = 20.dp)
                ) {
                    // Corner Brass Rivets
                    Box(modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) { ShopBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) { ShopBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)) { ShopBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)) { ShopBrassRivet() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Top Header Bar: Balances & Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Balances
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CandyBalanceBadge(amount = candies)
                                GemBalanceBadge(amount = gems)
                            }

                            // Close Button
                            Box(
                                modifier = Modifier
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
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Shop Awning / Mascot Banner
                        ShopAwningHeader()

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "BOOSTER SHOP",
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
                            text = "Power up your game & never get stuck!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8A7E6E),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Shop Item Rows
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Item 1: +3 Undos
                            ShopItemPlaqueRow(
                                title = "+3 Undos",
                                subtitle = "Rewind moves & restore board",
                                costText = "50",
                                isGems = false,
                                isFree = false,
                                canAfford = candies >= 50,
                                iconComposable = { UndoBoosterVectorIcon() },
                                onBuy = onBuyUndo
                            )

                            // Item 2: +3 Hints
                            ShopItemPlaqueRow(
                                title = "+3 Hints",
                                subtitle = "Reveal next solvable squishy",
                                costText = "75",
                                isGems = false,
                                isFree = false,
                                canAfford = candies >= 75,
                                iconComposable = { HintBoosterVectorIcon() },
                                onBuy = onBuyHint
                            )

                            // Item 3: +1 Magic Wand
                            ShopItemPlaqueRow(
                                title = "+1 Magic Wand",
                                subtitle = "Vaporize any asleep blocker",
                                costText = "100",
                                isGems = false,
                                isFree = false,
                                canAfford = candies >= 100,
                                iconComposable = { WandBoosterVectorIcon() },
                                onBuy = onBuyWand
                            )

                            // Item 4: Full Life Refill
                            ShopItemPlaqueRow(
                                title = "Full Life Refill",
                                subtitle = "Refill lives immediately to 5/5",
                                costText = "10",
                                isGems = true,
                                isFree = false,
                                canAfford = gems >= 10,
                                iconComposable = { HeartBoosterVectorIcon() },
                                onBuy = onRefillHeartsGems
                            )

                            // Item 5: Free Video Ad Heart
                            ShopItemPlaqueRow(
                                title = "Watch Ad: +1 Life",
                                subtitle = "Free instant recovery bonus",
                                costText = "FREE",
                                isGems = false,
                                isFree = true,
                                canAfford = true,
                                iconComposable = { AdVideoHeartVectorIcon() },
                                onBuy = onRefillHeartAd
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopItemPlaqueRow(
    title: String,
    subtitle: String,
    costText: String,
    isGems: Boolean,
    isFree: Boolean,
    canAfford: Boolean,
    iconComposable: @Composable () -> Unit,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rowShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, rowShape, spotColor = Color(0x22000000))
            .clip(rowShape)
            .background(Color(0xFFEDE3D2))
            .border(1.dp, Color(0xFFE2D4BF), rowShape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon + Titles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    iconComposable()
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
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A7E6E),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tactile 3D Extruded Purchase Button
            ShopExtrudedButton(
                costText = costText,
                isGems = isGems,
                isFree = isFree,
                enabled = canAfford,
                onClick = onBuy
            )
        }
    }
}

@Composable
private fun ShopExtrudedButton(
    costText: String,
    isGems: Boolean,
    isFree: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonCorner = RoundedCornerShape(16.dp)
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "ShopButtonPress"
    )

    val shadowBevelColor = when {
        !enabled -> Color(0xFF64748B)
        isFree -> Color(0xFFB45309)
        isGems -> Color(0xFF0369A1)
        else -> Color(0xFF065F46)
    }

    val faceGradient = when {
        !enabled -> listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))
        isFree -> listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
        isGems -> listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
        else -> listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669))
    }

    Box(
        modifier = Modifier
            .shadow(if (enabled) 4.dp else 0.dp, buttonCorner, spotColor = shadowBevelColor)
            .clip(buttonCorner)
            .background(shadowBevelColor)
            .padding(bottom = 3.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .offset(y = pressOffset)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 13.dp, bottomEnd = 13.dp))
                .background(Brush.verticalGradient(faceGradient))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isFree) {
                    Text(
                        text = "FREE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = costText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (isGems) {
                        MiniGemIcon()
                    } else {
                        MiniCandyIcon()
                    }
                }
            }
        }
    }
}

@Composable
private fun CandyBalanceBadge(amount: Int) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .shadow(2.dp, shape)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3))
                )
            )
            .border(1.dp, Color(0xFFFBCFE8), shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniCandyIcon()
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$amount",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SlateCharcoal
        )
    }
}

@Composable
private fun GemBalanceBadge(amount: Int) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .shadow(2.dp, shape)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFF0FDF4), Color(0xFFE0E7FF))
                )
            )
            .border(1.dp, Color(0xFFC7D2FE), shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniGemIcon()
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$amount",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SlateCharcoal
        )
    }
}

// ----------------- Custom Skia Canvas Vector Icons -----------------

@Composable
private fun UndoBoosterVectorIcon() {
    Canvas(modifier = Modifier.size(44.dp)) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Kiwi Jelly Circular Base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFA7F3D0), KiwiBase, Color(0xFF059669)),
                center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                radius = r * 1.1f
            ),
            radius = r,
            center = center
        )

        // Specular highlight
        drawOval(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(center.x - r * 0.5f, center.y - r * 0.8f),
            size = Size(r * 0.7f, r * 0.35f)
        )

        // Undo Arrow Arc
        val arrowPath = Path().apply {
            moveTo(center.x + r * 0.35f, center.y + r * 0.25f)
            cubicTo(
                center.x + r * 0.5f, center.y - r * 0.35f,
                center.x - r * 0.2f, center.y - r * 0.5f,
                center.x - r * 0.35f, center.y - r * 0.15f
            )
        }
        drawPath(
            path = arrowPath,
            color = Color.White,
            style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Arrow Head
        val headPath = Path().apply {
            moveTo(center.x - r * 0.55f, center.y - r * 0.18f)
            lineTo(center.x - r * 0.25f, center.y - r * 0.45f)
            lineTo(center.x - r * 0.25f, center.y + r * 0.05f)
            close()
        }
        drawPath(path = headPath, color = Color.White, style = Fill)
    }
}

@Composable
private fun HintBoosterVectorIcon() {
    Canvas(modifier = Modifier.size(44.dp)) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Strawberry Jelly Circular Base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFECDD3), StrawberryBase, Color(0xFFBE123C)),
                center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                radius = r * 1.1f
            ),
            radius = r,
            center = center
        )

        // Specular highlight
        drawOval(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(center.x - r * 0.5f, center.y - r * 0.8f),
            size = Size(r * 0.7f, r * 0.35f)
        )

        // Lightbulb Glowing Head
        drawCircle(
            color = Color(0xFFFEF08A),
            radius = r * 0.36f,
            center = Offset(center.x, center.y - r * 0.12f)
        )

        // Bulb Base
        drawRect(
            color = Color.White,
            topLeft = Offset(center.x - r * 0.18f, center.y + r * 0.18f),
            size = Size(r * 0.36f, r * 0.18f)
        )
        drawCircle(
            color = Color(0xFFCBD5E1),
            radius = r * 0.10f,
            center = Offset(center.x, center.y + r * 0.40f)
        )
    }
}

@Composable
private fun WandBoosterVectorIcon() {
    Canvas(modifier = Modifier.size(44.dp)) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Lemon Base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFEF9C3), LemonBase, Color(0xFFD97706)),
                center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                radius = r * 1.1f
            ),
            radius = r,
            center = center
        )

        // Specular highlight
        drawOval(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(center.x - r * 0.5f, center.y - r * 0.8f),
            size = Size(r * 0.7f, r * 0.35f)
        )

        // Wand Stick (Diagonal)
        drawLine(
            color = Color.White,
            start = Offset(center.x - r * 0.35f, center.y + r * 0.45f),
            end = Offset(center.x + r * 0.15f, center.y - r * 0.05f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Sparkle Star on Top Right
        val starCenter = Offset(center.x + r * 0.28f, center.y - r * 0.25f)
        val starR = r * 0.30f
        val starPath = Path().apply {
            moveTo(starCenter.x, starCenter.y - starR)
            quadraticTo(starCenter.x, starCenter.y, starCenter.x + starR, starCenter.y)
            quadraticTo(starCenter.x, starCenter.y, starCenter.x, starCenter.y + starR)
            quadraticTo(starCenter.x, starCenter.y, starCenter.x - starR, starCenter.y)
            quadraticTo(starCenter.x, starCenter.y, starCenter.x, starCenter.y - starR)
            close()
        }
        drawPath(path = starPath, color = Color.White, style = Fill)
    }
}

@Composable
private fun HeartBoosterVectorIcon() {
    Canvas(modifier = Modifier.size(44.dp)) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Coral Heart Gummy Base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFECDD3), CoralHeart, Color(0xFFBE123C)),
                center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                radius = r * 1.1f
            ),
            radius = r,
            center = center
        )

        // Specular highlight
        drawOval(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(center.x - r * 0.5f, center.y - r * 0.8f),
            size = Size(r * 0.7f, r * 0.35f)
        )

        // White Heart Motif in Center
        val hw = r * 0.9f
        val hh = r * 0.8f
        val ox = center.x
        val oy = center.y + r * 0.05f

        val path = Path().apply {
            moveTo(ox, oy + hh * 0.45f)
            cubicTo(ox - hw * 0.6f, oy + hh * 0.05f, ox - hw * 0.55f, oy - hh * 0.45f, ox, oy - hh * 0.20f)
            cubicTo(ox + hw * 0.55f, oy - hh * 0.45f, ox + hw * 0.6f, oy + hh * 0.05f, ox, oy + hh * 0.45f)
            close()
        }
        drawPath(path = path, color = Color.White, style = Fill)
    }
}

@Composable
private fun AdVideoHeartVectorIcon() {
    Canvas(modifier = Modifier.size(44.dp)) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Amber Base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFEF3C7), AmberGlow, Color(0xFFD97706)),
                center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                radius = r * 1.1f
            ),
            radius = r,
            center = center
        )

        // Specular highlight
        drawOval(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(center.x - r * 0.5f, center.y - r * 0.8f),
            size = Size(r * 0.7f, r * 0.35f)
        )

        // Play Triangle
        val playPath = Path().apply {
            moveTo(center.x - r * 0.25f, center.y - r * 0.32f)
            lineTo(center.x + r * 0.35f, center.y)
            lineTo(center.x - r * 0.25f, center.y + r * 0.32f)
            close()
        }
        drawPath(path = playPath, color = Color.White, style = Fill)
    }
}

@Composable
private fun MiniCandyIcon() {
    Canvas(modifier = Modifier.size(15.dp)) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Candy round swirl
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFF472B6), Color(0xFFDB2777)),
                center = center,
                radius = r
            ),
            radius = r,
            center = center
        )
        // Diagonal candy stripe
        drawCircle(color = Color.White.copy(alpha = 0.7f), radius = r * 0.45f, center = center)
        drawCircle(color = Color(0xFFDB2777), radius = r * 0.25f, center = center)
    }
}

@Composable
private fun MiniGemIcon() {
    Canvas(modifier = Modifier.size(15.dp)) {
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
private fun ShopAwningHeader(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 64.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height

        // Awning roof base
        val roofRect = androidx.compose.ui.geometry.RoundRect(
            rect = androidx.compose.ui.geometry.Rect(w * 0.1f, h * 0.15f, w * 0.9f, h * 0.75f),
            radiusX = 6.dp.toPx(),
            radiusY = 6.dp.toPx()
        )
        val roofPath = Path().apply { addRoundRect(roofRect) }
        drawPath(path = roofPath, color = Color(0xFFF43F5E), style = Fill)

        // Red & White Stripes
        val stripeWidth = (w * 0.8f) / 5f
        for (i in 0 until 5) {
            if (i % 2 == 1) {
                drawRect(
                    color = Color.White,
                    topLeft = Offset(w * 0.1f + i * stripeWidth, h * 0.15f),
                    size = Size(stripeWidth, h * 0.60f)
                )
            }
        }

        // Awning Scallops
        for (i in 0 until 5) {
            drawCircle(
                color = if (i % 2 == 1) Color.White else Color(0xFFF43F5E),
                radius = stripeWidth / 2f,
                center = Offset(w * 0.1f + i * stripeWidth + stripeWidth / 2f, h * 0.75f)
            )
        }
    }
}

@Composable
private fun ShopBrassRivet() {
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
