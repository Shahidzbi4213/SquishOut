package com.squishout.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.squishout.engine.model.DifficultyTier
import com.squishout.game.theme.BiomeTheme

/**
 * Carved golden-wood plaque with brass rivets and an inset gem capsule
 * displaying the current stage number and dynamic difficulty styling, matching the Stitch arcade visual style.
 */
@Composable
fun StageHeaderPlaque(
    stage: Int,
    diamonds: Int = 0,
    biome: BiomeTheme = BiomeTheme.SWEET_MEADOW,
    difficultyTier: DifficultyTier = DifficultyTier.NORMAL,
    modifier: Modifier = Modifier
) {
    val plaqueShape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .shadow(4.dp, plaqueShape)
            .clip(plaqueShape)
            // 3D bottom bevel rim
            .background(Color(0xFF8D4F0E))
            .padding(bottom = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        // Wooden/Golden Plaque Face
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 19.dp, bottomEnd = 19.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF9C35F), // Warm honey gold highlight
                            Color(0xFFEAA132), // Rich amber wood
                            Color(0xFFC47614)  // Golden wood base
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFFFE082),
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 19.dp, bottomEnd = 19.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left Brass Rivet
                BrassRivet()

                // Inset Dynamic Jewel Capsule
                InsetJewelCapsule(
                    stage = stage,
                    icon = biome.icon,
                    difficultyTier = difficultyTier
                )

                // Diamond Capsule with 💎 icon
                InsetDiamondCapsule(diamonds = diamonds)

                // Right Brass Rivet
                BrassRivet()
            }
        }
    }
}

@Composable
private fun InsetDiamondCapsule(diamonds: Int) {
    val capsuleShape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .height(34.dp)
            .shadow(2.dp, capsuleShape)
            .clip(capsuleShape)
            .background(Color(0xFF0369A1))
            .padding(bottom = 1.5.dp)
    ) {
        Box(
            modifier = Modifier
                .height(32.5.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0x66FFFFFF),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "💎",
                    fontSize = 13.sp
                )
                Text(
                    text = "$diamonds",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0x990369A1),
                            offset = Offset(0f, 2f),
                            blurRadius = 3f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun InsetJewelCapsule(
    stage: Int,
    icon: String,
    difficultyTier: DifficultyTier = DifficultyTier.NORMAL
) {
    val capsuleShape = RoundedCornerShape(16.dp)

    val rimColor: Color
    val gemColors: List<Color>
    val textShadowColor: Color
    val displayText: String
    val displayIcon: String
    val dotColor: Color

    when (difficultyTier) {
        DifficultyTier.SUPER_HARD -> {
            rimColor = Color(0xFF3B0764)
            gemColors = listOf(Color(0xFFA855F7), Color(0xFF7E22CE), Color(0xFF581C87))
            textShadowColor = Color(0x992E1065)
            displayText = "Boss $stage"
            displayIcon = "👑"
            dotColor = Color(0xFFFFD700)
        }
        DifficultyTier.HARD -> {
            rimColor = Color(0xFF450A0A)
            gemColors = listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF991B1B))
            textShadowColor = Color(0x99450A0A)
            displayText = "Hard $stage"
            displayIcon = "🔥"
            dotColor = Color(0xFFFEF08A)
        }
        DifficultyTier.BREATHER -> {
            rimColor = Color(0xFF064E3B)
            gemColors = listOf(Color(0xFF34D399), Color(0xFF059669), Color(0xFF047857))
            textShadowColor = Color(0x77064E3B)
            displayText = "Stage $stage"
            displayIcon = "✨"
            dotColor = Color(0xFFA7F3D0)
        }
        DifficultyTier.NORMAL -> {
            rimColor = Color(0xFF00382B)
            gemColors = listOf(Color(0xFF00BFA5), Color(0xFF00897B), Color(0xFF004D40))
            textShadowColor = Color(0x77002D24)
            displayText = "Stage $stage"
            displayIcon = icon
            dotColor = Color(0xFFFFE082)
        }
    }

    Box(
        modifier = Modifier
            .height(34.dp)
            .widthIn(min = 120.dp)
            .shadow(2.dp, capsuleShape)
            .clip(capsuleShape)
            // Bevel rim for inset effect
            .background(rimColor)
            .padding(bottom = 1.5.dp)
    ) {
        // Face of the gem
        Box(
            modifier = Modifier
                .height(32.5.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = gemColors
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0x66FFFFFF),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            // Top specular crescent highlight
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 30f
                        )
                    )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = displayIcon,
                    fontSize = 15.sp
                )
                Text(
                    text = displayText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = textShadowColor,
                            offset = Offset(0f, 2f),
                            blurRadius = 3f
                        )
                    )
                )
                // Gleaming golden jewel dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun BrassRivet() {
    Canvas(modifier = Modifier.size(7.dp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer dark brass ring
        drawCircle(
            color = Color(0xFF6D3800),
            radius = radius,
            center = center
        )
        // Raised polished brass dome
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFEE99),
                    Color(0xFFE5A823),
                    Color(0xFF8A5200)
                ),
                center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                radius = radius * 0.9f
            ),
            radius = radius * 0.85f,
            center = center
        )
    }
}
