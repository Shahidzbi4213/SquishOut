package com.squishout.game.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A tactile 3D candy gummy toggle switch matching the Squish Out arcade aesthetic.
 * Features a recessed pill track with inner depth and a bouncing glossy jelly knob
 * that squishes and stretches upon toggling.
 */
@Composable
fun GummyToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bouncy spring animation for knob translation
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 28.dp else 3.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "GummyThumbOffset"
    )

    // Squish-and-stretch scale when held / toggling
    val squishX by animateFloatAsState(
        targetValue = if (isPressed) 1.22f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "GummySquishX"
    )
    val squishY by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "GummySquishY"
    )

    val trackShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .size(width = 58.dp, height = 32.dp)
            .shadow(
                elevation = if (checked) 2.dp else 1.dp,
                shape = trackShape,
                spotColor = if (checked) Color(0xFF059669) else Color.Black.copy(alpha = 0.15f)
            )
            .clip(trackShape)
            // Outer 3D bevel rim
            .background(
                if (checked) Color(0xFF047857) else Color(0xFF94A3B8)
            )
            .padding(1.dp)
            // Track inner well
            .background(
                if (checked) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D9488),
                            Color(0xFF10B981),
                            Color(0xFF059669)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFCBD5E1),
                            Color(0xFFE2E8F0),
                            Color(0xFFF1F5F9)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (checked) Color(0xFF34D399).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f),
                shape = trackShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track text indicator (ON / OFF)
        if (checked) {
            Text(
                text = "ON",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 7.dp)
            )
        } else {
            Text(
                text = "OFF",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
            )
        }

        // Bouncy 3D Gummy Knob
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(26.dp)
                .scale(scaleX = squishX, scaleY = squishY)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                // Bottom bevel shadow rim of knob
                .background(
                    if (checked) Color(0xFF065F46) else Color(0xFF64748B)
                )
                .padding(bottom = 2.dp)
                // Knob face
                .clip(CircleShape)
                .background(
                    if (checked) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFA7F3D0),
                                Color(0xFF34D399),
                                Color(0xFF059669)
                            ),
                            center = Offset(8f, 8f),
                            radius = 35f
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF8FAFC),
                                Color(0xFFE2E8F0),
                                Color(0xFFCBD5E1)
                            ),
                            center = Offset(8f, 8f),
                            radius = 35f
                        )
                    }
                )
        ) {
            // Specular Arc Highlight
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = size.minDimension * 0.22f,
                    center = Offset(size.width * 0.35f, size.height * 0.35f)
                )
            }
        }
    }
}
