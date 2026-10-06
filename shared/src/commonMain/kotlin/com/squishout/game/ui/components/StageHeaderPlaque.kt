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
import com.squishout.game.theme.BiomeTheme

/**
 * Carved golden-wood plaque with brass rivets and an inset emerald/cyan jewel capsule
 * displaying the current stage number, matching the Stitch arcade visual style.
 */
@Composable
fun StageHeaderPlaque(
    stage: Int,
    biome: BiomeTheme = BiomeTheme.SWEET_MEADOW,
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

                // Inset Emerald Jewel Capsule
                InsetJewelCapsule(stage = stage, icon = biome.icon)

                // Right Brass Rivet
                BrassRivet()
            }
        }
    }
}

@Composable
private fun InsetJewelCapsule(stage: Int, icon: String) {
    val capsuleShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .height(34.dp)
            .widthIn(min = 120.dp)
            .shadow(2.dp, capsuleShape)
            .clip(capsuleShape)
            // Bevel rim for inset effect
            .background(Color(0xFF00382B))
            .padding(bottom = 1.5.dp)
    ) {
        // Face of the gem
        Box(
            modifier = Modifier
                .height(32.5.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00BFA5), // Teal/emerald bright gem
                            Color(0xFF00897B), // Deep teal
                            Color(0xFF004D40)  // Inset shade
                        )
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
                    text = icon,
                    fontSize = 15.sp
                )
                Text(
                    text = "Stage $stage",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0x77002D24),
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
                        .background(Color(0xFFFFE082))
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

        // Outer bronze shadow rim
        drawCircle(
            color = Color(0xFF6D3804),
            radius = radius,
            center = center
        )

        // Inner golden dome
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFDE7),
                    Color(0xFFFFD54F),
                    Color(0xFFD97706)
                ),
                center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                radius = radius * 0.9f
            ),
            radius = radius * 0.82f,
            center = center
        )
    }
}
