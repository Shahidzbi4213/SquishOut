package com.squishout.game.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.data.entity.JellySkinEntity
import com.squishout.game.data.entity.UserSessionEntity
import com.squishout.game.theme.AmberGlow
import com.squishout.game.theme.BlueberryBase
import com.squishout.game.theme.EmeraldMint
import com.squishout.game.theme.FrostedWhite
import com.squishout.game.theme.GrapeBase
import com.squishout.game.theme.KiwiBase
import com.squishout.game.theme.LemonBase
import com.squishout.game.theme.SlateCharcoal
import com.squishout.game.ui.components.SagaBottomNav
import com.squishout.game.theme.StrawberryBase
import com.squishout.game.theme.StrawberryGloss

@Composable
fun JellyDexScreen(
    session: UserSessionEntity,
    skins: List<JellySkinEntity>,
    onEquipSkin: (String) -> Unit,
    onUnlockSkin: (String, Int) -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToShop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val equippedSkin = skins.firstOrNull { it.isEquipped } ?: skins.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8F5E9), // Soft Mint
                        Color(0xFFF1F5F9), // Slate White
                        Color(0xFFFFF7ED)  // Warm Cream
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            DexHeader(session = session, skinsCount = skins.count { it.isUnlocked }, totalCount = skins.size)

            // Grid content
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Hero Card Span
                if (equippedSkin != null) {
                    item(span = { GridItemSpan(2) }) {
                        HeroMascotCard(skin = equippedSkin)
                    }
                }

                // Section Title
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🐾 SQUISHY COLLECTION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SlateCharcoal.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tap to equip",
                            fontSize = 11.sp,
                            color = EmeraldMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Grid items
                items(skins) { skin ->
                    SkinCard(
                        skin = skin,
                        onEquip = { onEquipSkin(skin.jellyId) },
                        onUnlock = { onUnlockSkin(skin.jellyId, 25) }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }

        // Floating Toy-Like Arcade Navigation Dock
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            SagaBottomNav(
                selectedTab = "dex",
                onMapClick = onNavigateToMap,
                onDexClick = {},
                onShopClick = onNavigateToShop
            )
        }
    }
}

@Composable
private fun DexHeader(session: UserSessionEntity, skinsCount: Int, totalCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .displayCutoutPadding()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SQUISH OUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = SlateCharcoal.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Jelly Dex",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateCharcoal
                )
            }

            // Diamonds Balance Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(FrostedWhite)
                    .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💎", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${session.diamonds}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0369A1)
                )
            }
        }
    }
}

@Composable
private fun HeroMascotCard(skin: JellySkinEntity) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedWhite),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = StrawberryBase.copy(alpha = 0.25f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            // Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF2F2))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⭐ ACTIVE COMPANION • ${skin.rarity}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = StrawberryBase,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = skin.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = SlateCharcoal
            )

            Text(
                text = skin.perkDescription ?: "Juicy companion with bouncy squish power",
                fontSize = 12.sp,
                color = SlateCharcoal.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Hero Mascot Canvas
            HeroMascotCanvas(
                jellyId = skin.jellyId,
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Equipped Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(EmeraldMint)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓ EQUIPPED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SkinCard(
    skin: JellySkinEntity,
    onEquip: () -> Unit,
    onUnlock: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedWhite),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(14.dp)
        ) {
            // Rarity Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (skin.rarity) {
                            "MYTHIC" -> Color(0xFFFAF5FF)
                            "RARE" -> Color(0xFFF0FDF4)
                            else -> Color(0xFFF8FAFC)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = skin.rarity,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = when (skin.rarity) {
                        "MYTHIC" -> GrapeBase
                        "RARE" -> EmeraldMint
                        else -> SlateCharcoal.copy(alpha = 0.6f)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini Jelly Icon
            MiniJellyIcon(jellyId = skin.jellyId, modifier = Modifier.size(54.dp))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = skin.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SlateCharcoal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Button Action
            when {
                skin.isEquipped -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EQUIPPED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateCharcoal.copy(alpha = 0.6f)
                        )
                    }
                }
                skin.isUnlocked -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldMint)
                            .clickable { onEquip() }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EQUIP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                )
                            )
                            .clickable { onUnlock() }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💎 25",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMascotCanvas(jellyId: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f + floatOffset
        val radius = size.width * 0.38f

        // Soft pedestal glow
        drawOval(
            color = Color(0xFFBBF7D0).copy(alpha = 0.5f),
            topLeft = Offset(cx - radius * 1.1f, size.height - 14.dp.toPx()),
            size = Size(radius * 2.2f, 12.dp.toPx())
        )

        // Radial gummy body
        val colors = when {
            jellyId.contains("strawberry") -> listOf(StrawberryGloss, StrawberryBase, Color(0xFF991B1B))
            jellyId.contains("blueberry") -> listOf(Color(0xFF60A5FA), BlueberryBase, Color(0xFF1E3A8A))
            jellyId.contains("lemon") -> listOf(Color(0xFFFEF08A), LemonBase, Color(0xFFCA8A04))
            jellyId.contains("kiwi") -> listOf(Color(0xFF86EFAC), KiwiBase, Color(0xFF15803D))
            jellyId.contains("cosmic") -> listOf(Color(0xFFF472B6), Color(0xFF8B5CF6), Color(0xFF1E1B4B))
            else -> listOf(Color(0xFFD8B4FE), GrapeBase, Color(0xFF6B21A8))
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = colors,
                center = Offset(cx - radius * 0.25f, cy - radius * 0.25f),
                radius = radius * 1.2f
            ),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Specular gloss
        drawOval(
            color = Color.White.copy(alpha = 0.7f),
            topLeft = Offset(cx - radius * 0.55f, cy - radius * 0.7f),
            size = Size(radius * 0.75f, radius * 0.35f)
        )

        // Eyes: Sparkling happy awake pupils ( ✦‿✦ )
        val eyeRadius = radius * 0.16f
        val eyeY = cy - radius * 0.05f
        val leftX = cx - radius * 0.35f
        val rightX = cx + radius * 0.35f

        drawCircle(color = Color(0xFF0F172A), radius = eyeRadius, center = Offset(leftX, eyeY))
        drawCircle(color = Color.White, radius = eyeRadius * 0.45f, center = Offset(leftX - eyeRadius * 0.2f, eyeY - eyeRadius * 0.2f))

        drawCircle(color = Color(0xFF0F172A), radius = eyeRadius, center = Offset(rightX, eyeY))
        drawCircle(color = Color.White, radius = eyeRadius * 0.45f, center = Offset(rightX - eyeRadius * 0.2f, eyeY - eyeRadius * 0.2f))

        // Cute smiling mouth
        val mouthPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx - radius * 0.18f, cy + radius * 0.2f)
            quadraticTo(cx, cy + radius * 0.38f, cx + radius * 0.18f, cy + radius * 0.2f)
        }
        drawPath(
            path = mouthPath,
            color = Color(0xFF0F172A),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 3.5f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

@Composable
private fun MiniJellyIcon(jellyId: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width * 0.4f

        val colors = when {
            jellyId.contains("strawberry") -> listOf(StrawberryGloss, StrawberryBase)
            jellyId.contains("blueberry") -> listOf(Color(0xFF60A5FA), BlueberryBase)
            jellyId.contains("lemon") -> listOf(Color(0xFFFEF08A), LemonBase)
            jellyId.contains("kiwi") -> listOf(Color(0xFF86EFAC), KiwiBase)
            jellyId.contains("cosmic") -> listOf(Color(0xFFF472B6), Color(0xFF8B5CF6))
            else -> listOf(Color(0xFFD8B4FE), GrapeBase)
        }

        drawCircle(
            brush = Brush.radialGradient(colors),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Gloss
        drawOval(
            color = Color.White.copy(alpha = 0.65f),
            topLeft = Offset(cx - radius * 0.5f, cy - radius * 0.65f),
            size = Size(radius * 0.65f, radius * 0.3f)
        )

        // Eyes
        val eyeRadius = radius * 0.14f
        drawCircle(color = Color(0xFF0F172A), radius = eyeRadius, center = Offset(cx - radius * 0.3f, cy))
        drawCircle(color = Color(0xFF0F172A), radius = eyeRadius, center = Offset(cx + radius * 0.3f, cy))
    }
}
