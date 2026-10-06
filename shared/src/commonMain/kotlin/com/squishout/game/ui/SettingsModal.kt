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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squishout.game.theme.SlateCharcoal
import com.squishout.game.ui.components.DozingMascotHeader
import com.squishout.game.ui.components.GummyToggleSwitch
import com.squishout.game.ui.components.ModalButtonVariant
import com.squishout.game.ui.components.ModalExtrudedButton
import com.squishout.game.ui.components.ModalMapIcon
import com.squishout.game.ui.components.ModalPlayIcon
import com.squishout.game.ui.components.ModalRestartIcon
import com.squishout.game.ui.components.SoundSpeakerIcon
import com.squishout.game.ui.components.VibrateWaveIcon

/**
 * Casual Game 3D Settings & Pause Modal ("Gummy Arcade Pause Plaque").
 * Replaces generic flat forms with a glazed ceramic porcelain plaque featuring:
 * - Ambient backdrop scrim & glowing warm radial halo
 * - A sleepy dozing Strawberry Blob mascot with gentle breathing and floating Zzz dream bubbles
 * - 4 decorative corner brass rivets
 * - Recessed soft-cream control capsules with tactile 3D gummy rocker switches
 * - Bespoke Canvas vector icons (no cheap system emojis)
 * - 3D extruded push buttons with bottom shadow bevels and physical press depression
 */
@Composable
fun SettingsModal(
    isVisible: Boolean,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExitToMap: () -> Unit,
    stage: Int = 1,
    hearts: Int = 3,
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
                .background(Color(0xFF0F1E16).copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onResume() },
            contentAlignment = Alignment.Center
        ) {
            // Ambient Radial Glow Halo
            Box(
                modifier = Modifier
                    .size(360.dp)
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

            // Porcelain Ceramic Plaque Container
            val plaqueShape = RoundedCornerShape(32.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .widthIn(max = 380.dp)
                    .shadow(16.dp, plaqueShape, spotColor = Color(0xFF6D3804))
                    .clip(plaqueShape)
                    // 3D bottom wood/caramel rim bevel
                    .background(Color(0xFF8D4F0E))
                    .padding(bottom = 5.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Prevent dismiss when clicking plaque */ },
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
                                    Color(0xFFFFFDF8), // High gloss porcelain cream
                                    Color(0xFFFBF4E8),
                                    Color(0xFFF5E8D6)  // Warm caramel undertone
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = Color(0xFFFFE8D1),
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 27.dp, bottomEnd = 27.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    // Four Corner Brass Rivets
                    Box(modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) { ModalBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) { ModalBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)) { ModalBrassRivet() }
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)) { ModalBrassRivet() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 1. Dozing Mascot Animated Header
                        DozingMascotHeader(stage = stage)

                        Spacer(modifier = Modifier.height(18.dp))

                        // 2. Settings Controls Capsule (Inset Well)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Sound Effects Toggle Row
                            ArcadeSettingRow(
                                title = "Sound Effects",
                                subtitle = "Ploinks, pops & fanfare",
                                icon = { SoundSpeakerIcon(color = Color(0xFF6D4321)) },
                                checked = soundEnabled,
                                onCheckedChange = onToggleSound
                            )

                            // Haptics Toggle Row
                            ArcadeSettingRow(
                                title = "Vibration & Haptics",
                                subtitle = "Tactile clicks & thumps",
                                icon = { VibrateWaveIcon(color = Color(0xFF6D4321)) },
                                checked = hapticsEnabled,
                                onCheckedChange = onToggleHaptics
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 3. Tactile 3D Action Push Buttons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Primary Resume CTA (Emerald Mint)
                            ModalExtrudedButton(
                                text = "▶  RESUME GAME",
                                variant = ModalButtonVariant.EMERALD,
                                onClick = onResume,
                                leadingIcon = { ModalPlayIcon() }
                            )

                            // Restart Level (Cream Porcelain)
                            ModalExtrudedButton(
                                text = "Restart Level",
                                variant = ModalButtonVariant.CREAM,
                                onClick = onRestart,
                                leadingIcon = { ModalRestartIcon() }
                            )

                            // Exit to Saga Map (Coral Berry)
                            ModalExtrudedButton(
                                text = "Exit to Map",
                                variant = ModalButtonVariant.CORAL,
                                onClick = onExitToMap,
                                leadingIcon = { ModalMapIcon() }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Subtle Footer Info Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Squish Out • v1.0.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFA89F91)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Inset soft-cream control row featuring tactile typography and a 3D GummyToggleSwitch.
 */
@Composable
private fun ArcadeSettingRow(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val rowShape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, rowShape)
            .clip(rowShape)
            // Recessed soft cream container
            .background(Color(0xFFEDE3D2))
            .border(width = 1.dp, color = Color(0xFFE2D4BF), shape = rowShape)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon pill badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFFDF8))
                        .border(1.dp, Color(0xFFDFCDBD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateCharcoal
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8A7E6E)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3D Bouncy Gummy Toggle
            GummyToggleSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

/**
 * Decorative 3D brass rivet for plaque corners.
 */
@Composable
private fun ModalBrassRivet() {
    Canvas(modifier = Modifier.size(8.dp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer bronze shadow rim
        drawCircle(
            color = Color(0xFF6D3804),
            radius = radius,
            center = center
        )

        // Inner golden dome with radial highlight
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
