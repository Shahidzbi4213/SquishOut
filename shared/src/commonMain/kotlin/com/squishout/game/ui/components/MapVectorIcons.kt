package com.squishout.game.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * AAA Arcade Vector Graphics Collection for the Saga Map screen.
 * Replaces all cheap unicode emojis (🍓, ❤️, 🎁, ★, 👑, 🔒, 🏰, 🍭, 🌸, 🍬, 🍄, ✨)
 * with bespoke Canvas-drawn 3D candy game assets matching SquishOut's design system.
 */

/* =========================================================================
   1. KAWAII STRAWBERRY MASCOT (With Optional Royal Golden Crown)
   ========================================================================= */

@Composable
fun CanvasStrawberryMascot(
    modifier: Modifier = Modifier,
    withCrown: Boolean = false,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_idle")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (animated) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_breathe"
    )

    Canvas(modifier = modifier.scale(breatheScale)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * (if (withCrown) 0.58f else 0.50f)
        val r = w * 0.40f

        // 1. Drop shadow beneath mascot
        drawOval(
            color = Color(0xFF670020).copy(alpha = 0.35f),
            topLeft = Offset(cx - r * 0.85f, cy + r * 0.65f),
            size = Size(r * 1.7f, r * 0.45f)
        )

        // 2. Strawberry Body Path (Tapered teardrop gummy bean)
        val bodyPath = Path().apply {
            moveTo(cx, cy - r * 0.90f)
            cubicTo(cx + r * 1.05f, cy - r * 0.90f, cx + r * 1.05f, cy + r * 0.40f, cx + r * 0.45f, cy + r * 0.90f)
            cubicTo(cx + r * 0.20f, cy + r * 1.05f, cx - r * 0.20f, cy + r * 1.05f, cx - r * 0.45f, cy + r * 0.90f)
            cubicTo(cx - r * 1.05f, cy + r * 0.40f, cx - r * 1.05f, cy - r * 0.90f, cx, cy - r * 0.90f)
            close()
        }

        // Body 3D Radial Gummy Fill
        drawPath(
            path = bodyPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF758F), // Gloss highlight
                    Color(0xFFFF3366), // Mid vibrant coral
                    Color(0xFFC9184A), // Rich shade
                    Color(0xFF800F2F)  // Deep shadow edge
                ),
                center = Offset(cx - r * 0.30f, cy - r * 0.25f),
                radius = r * 1.35f
            )
        )

        // Specular Gloss Highlight Crescent on Top-Left
        drawOval(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.85f), Color.Transparent)
            ),
            topLeft = Offset(cx - r * 0.70f, cy - r * 0.80f),
            size = Size(r * 0.80f, r * 0.40f)
        )

        // Green Leaf Calyx on Top
        val leafY = cy - r * 0.85f
        val leftLeaf = Path().apply {
            moveTo(cx, leafY)
            quadraticTo(cx - r * 0.65f, leafY - r * 0.35f, cx - r * 0.50f, leafY)
            close()
        }
        val rightLeaf = Path().apply {
            moveTo(cx, leafY)
            quadraticTo(cx + r * 0.65f, leafY - r * 0.35f, cx + r * 0.50f, leafY)
            close()
        }
        val centerLeaf = Path().apply {
            moveTo(cx - r * 0.18f, leafY)
            quadraticTo(cx, leafY - r * 0.50f, cx + r * 0.18f, leafY)
            close()
        }
        drawPath(path = leftLeaf, color = Color(0xFF2DC653))
        drawPath(path = rightLeaf, color = Color(0xFF2DC653))
        drawPath(path = centerLeaf, color = Color(0xFF38B000))

        // Cute Blush Cheeks
        drawCircle(
            color = Color(0xFFFFB3C1).copy(alpha = 0.75f),
            radius = r * 0.16f,
            center = Offset(cx - r * 0.44f, cy + r * 0.12f)
        )
        drawCircle(
            color = Color(0xFFFFB3C1).copy(alpha = 0.75f),
            radius = r * 0.16f,
            center = Offset(cx + r * 0.44f, cy + r * 0.12f)
        )

        // Kawaii Big Eyes with Catchlights
        val eyeRadius = r * 0.14f
        val eyeY = cy - r * 0.04f
        val leftEyeX = cx - r * 0.28f
        val rightEyeX = cx + r * 0.28f

        drawCircle(color = Color(0xFF1E1014), radius = eyeRadius, center = Offset(leftEyeX, eyeY))
        drawCircle(color = Color(0xFF1E1014), radius = eyeRadius, center = Offset(rightEyeX, eyeY))

        // Eye highlights
        drawCircle(color = Color.White, radius = eyeRadius * 0.50f, center = Offset(leftEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))
        drawCircle(color = Color.White, radius = eyeRadius * 0.24f, center = Offset(leftEyeX + eyeRadius * 0.30f, eyeY + eyeRadius * 0.25f))
        drawCircle(color = Color.White, radius = eyeRadius * 0.50f, center = Offset(rightEyeX - eyeRadius * 0.25f, eyeY - eyeRadius * 0.25f))
        drawCircle(color = Color.White, radius = eyeRadius * 0.24f, center = Offset(rightEyeX + eyeRadius * 0.30f, eyeY + eyeRadius * 0.25f))

        // Sweet Smile Mouth
        val mouthPath = Path().apply {
            moveTo(cx - r * 0.14f, cy + r * 0.18f)
            quadraticTo(cx, cy + r * 0.36f, cx + r * 0.14f, cy + r * 0.18f)
        }
        drawPath(
            path = mouthPath,
            color = Color(0xFF1E1014),
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Royal 3-Point Golden Crown (If withCrown is requested)
        if (withCrown) {
            val crownW = r * 1.15f
            val crownH = r * 0.70f
            val crownBaseY = leafY + r * 0.05f

            val crownPath = Path().apply {
                moveTo(cx - crownW / 2f, crownBaseY)
                lineTo(cx - crownW / 2f, crownBaseY - crownH * 0.65f)
                lineTo(cx - crownW * 0.22f, crownBaseY - crownH * 0.35f)
                lineTo(cx, crownBaseY - crownH)
                lineTo(cx + crownW * 0.22f, crownBaseY - crownH * 0.35f)
                lineTo(cx + crownW / 2f, crownBaseY - crownH * 0.65f)
                lineTo(cx + crownW / 2f, crownBaseY)
                close()
            }

            // Crown drop shadow
            drawPath(
                path = crownPath,
                color = Color(0xFF78350F).copy(alpha = 0.40f)
            )

            // Crown golden radial fill
            drawPath(
                path = crownPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFDE7),
                        Color(0xFFFFD54F),
                        Color(0xFFF59E0B),
                        Color(0xFFD97706)
                    ),
                    center = Offset(cx, crownBaseY - crownH * 0.5f),
                    radius = crownW
                )
            )

            // Crown rim stroke
            drawPath(
                path = crownPath,
                color = Color(0xFFFFF9DB),
                style = Stroke(width = 1.2.dp.toPx(), join = StrokeJoin.Round)
            )

            // Ruby Jewels on the 3 Crown Peaks
            val peakLeft = Offset(cx - crownW / 2f, crownBaseY - crownH * 0.65f)
            val peakCenter = Offset(cx, crownBaseY - crownH)
            val peakRight = Offset(cx + crownW / 2f, crownBaseY - crownH * 0.65f)

            for (peak in listOf(peakLeft, peakCenter, peakRight)) {
                drawCircle(color = Color(0xFFE11D48), radius = r * 0.10f, center = peak)
                drawCircle(color = Color.White.copy(alpha = 0.85f), radius = r * 0.04f, center = Offset(peak.x - r * 0.03f, peak.y - r * 0.03f))
            }
        }
    }
}

/* =========================================================================
   2. 3D GLOSSY RUBY CANDY HEART (Zero Emojis)
   ========================================================================= */

@Composable
fun CanvasRubyHeart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.90f)
            cubicTo(w * 0.08f, h * 0.60f, -w * 0.04f, h * 0.22f, w * 0.24f, h * 0.08f)
            cubicTo(w * 0.38f, -h * 0.02f, w * 0.50f, h * 0.15f, w * 0.50f, h * 0.22f)
            cubicTo(w * 0.50f, h * 0.15f, w * 0.62f, -h * 0.02f, w * 0.76f, h * 0.08f)
            cubicTo(w * 1.04f, h * 0.22f, w * 0.92f, h * 0.60f, w * 0.5f, h * 0.90f)
            close()
        }

        // Bottom bevel shadow
        drawPath(
            path = heartPath,
            color = Color(0xFF590214).copy(alpha = 0.40f)
        )

        // Ruby Candy Gradient
        drawPath(
            path = heartPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF758F),
                    Color(0xFFFF3366),
                    Color(0xFFE63946),
                    Color(0xFF800F2F)
                ),
                center = Offset(w * 0.35f, h * 0.30f),
                radius = w * 0.75f
            )
        )

        // Left lobe specular crescent
        drawOval(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.85f), Color.Transparent)
            ),
            topLeft = Offset(w * 0.18f, h * 0.12f),
            size = Size(w * 0.26f, h * 0.26f)
        )

        // Right lobe tiny specular dot
        drawCircle(
            color = Color.White.copy(alpha = 0.70f),
            radius = w * 0.06f,
            center = Offset(w * 0.72f, h * 0.22f)
        )
    }
}

/* =========================================================================
   3. 3D GOLDEN GIFT CHEST (Zero Emojis)
   ========================================================================= */

@Composable
fun CanvasGiftChest(
    modifier: Modifier = Modifier,
    canClaim: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gift_bounce")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (canClaim) -4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Canvas(modifier = modifier.scale(if (canClaim) 1.05f else 1.0f)) {
        val w = size.width
        val h = size.height

        val boxL = w * 0.16f
        val boxR = w * 0.84f
        val boxT = h * 0.35f
        val boxB = h * 0.86f
        val boxW = boxR - boxL
        val boxH = boxB - boxT

        // 1. Box Bottom Drop Shadow
        drawRoundRect(
            color = Color(0xFF6D3804).copy(alpha = 0.40f),
            topLeft = Offset(boxL, boxB - 2.dp.toPx()),
            size = Size(boxW, 5.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // 2. Main Box Base Container (Golden Honey Enamel)
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFFF3B0), Color(0xFFFFB703), Color(0xFFD97706))
            ),
            topLeft = Offset(boxL, boxT),
            size = Size(boxW, boxH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFFFFF9DB),
            topLeft = Offset(boxL, boxT),
            size = Size(boxW, boxH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )

        // 3. Lid Flap (Slightly wider top cap)
        val lidL = w * 0.12f
        val lidR = w * 0.88f
        val lidT = h * 0.22f
        val lidB = h * 0.38f
        val lidW = lidR - lidL
        val lidH = lidB - lidT

        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFFF9DB), Color(0xFFFFC300), Color(0xFFE85D04))
            ),
            topLeft = Offset(lidL, lidT),
            size = Size(lidW, lidH),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFFFFFBEA),
            topLeft = Offset(lidL, lidT),
            size = Size(lidW, lidH),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )

        // 4. Crimson Silk Ribbon Bands (Vertical)
        val ribbonW = boxW * 0.28f
        val ribbonL = w * 0.5f - ribbonW / 2f
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFFF4D6D), Color(0xFFE63946), Color(0xFFC9184A))
            ),
            topLeft = Offset(ribbonL, lidT),
            size = Size(ribbonW, boxB - lidT)
        )

        // 5. 3D Ribbon Bow on Top
        val bowCenter = Offset(w * 0.5f, lidT)
        val bowLoopR = w * 0.16f

        // Left bow loop
        drawOval(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFF758F), Color(0xFFE63946)),
                center = Offset(bowCenter.x - bowLoopR, bowCenter.y - bowLoopR * 0.5f)
            ),
            topLeft = Offset(bowCenter.x - bowLoopR * 1.5f, bowCenter.y - bowLoopR),
            size = Size(bowLoopR * 1.5f, bowLoopR * 1.1f)
        )
        // Right bow loop
        drawOval(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFF758F), Color(0xFFE63946)),
                center = Offset(bowCenter.x + bowLoopR, bowCenter.y - bowLoopR * 0.5f)
            ),
            topLeft = Offset(bowCenter.x, bowCenter.y - bowLoopR),
            size = Size(bowLoopR * 1.5f, bowLoopR * 1.1f)
        )
        // Center bow knot
        drawCircle(
            color = Color(0xFFFFF0F3),
            radius = bowLoopR * 0.45f,
            center = bowCenter
        )
        drawCircle(
            color = Color(0xFFC9184A),
            radius = bowLoopR * 0.38f,
            center = bowCenter
        )
    }
}

/* =========================================================================
   4. 3D EMBOSSED GOLDEN CANDY STARS (Zero Unicode Emojis)
   ========================================================================= */

@Composable
fun Canvas3DStar(
    isEarned: Boolean = true,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val outerR = w * 0.46f
        val innerR = outerR * 0.42f

        // 5-point star polygon path
        val starPath = Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = -PI / 2 + i * (PI / 5)
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()

        if (isEarned) {
            // 3D bottom shadow
            val shadowPath = Path()
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = -PI / 2 + i * (PI / 5)
                val x = (cx + r * cos(angle)).toFloat()
                val y = (cy + 1.6.dp.toPx() + r * sin(angle)).toFloat()
                if (i == 0) shadowPath.moveTo(x, y) else shadowPath.lineTo(x, y)
            }
            shadowPath.close()
            drawPath(path = shadowPath, color = Color(0xFF92400E))

            // Star Radiant Gold Face
            drawPath(
                path = starPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFDE7), // Specular point
                        Color(0xFFFFD54F), // Bright gold
                        Color(0xFFF59E0B), // Warm amber
                        Color(0xFFD97706)  // Depth edge
                    ),
                    center = Offset(cx, cy - outerR * 0.25f),
                    radius = outerR * 1.2f
                )
            )

            // Embossed Facet Crease Lines (Center to outer tips)
            for (i in 0 until 5) {
                val angle = -PI / 2 + (i * 2) * (PI / 5)
                val tipX = (cx + outerR * cos(angle)).toFloat()
                val tipY = (cy + outerR * sin(angle)).toFloat()
                drawLine(
                    color = Color.White.copy(alpha = 0.65f),
                    start = Offset(cx, cy),
                    end = Offset(tipX, tipY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Top Tip Specular Sparkle Dot
            drawCircle(
                color = Color.White,
                radius = 1.4.dp.toPx(),
                center = Offset(cx, cy - outerR * 0.70f)
            )
        } else {
            // Recessed Matte Well (Empty / Unearned star)
            drawPath(
                path = starPath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8))
                )
            )
            drawPath(
                path = starPath,
                color = Color(0xFF64748B).copy(alpha = 0.40f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

/* =========================================================================
   5. 3D GOLDEN BRASS PADLOCK (Zero Emojis)
   ========================================================================= */

@Composable
fun CanvasGoldenPadlock(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val bodyW = w * 0.72f
        val bodyH = h * 0.52f
        val bodyL = (w - bodyW) / 2f
        val bodyT = h * 0.42f

        // Shackle Steel Arc
        val shackleRadius = bodyW * 0.30f
        val shackleCenterX = w / 2f
        val shackleCenterY = bodyT

        val shacklePath = Path().apply {
            moveTo(shackleCenterX - shackleRadius, shackleCenterY)
            lineTo(shackleCenterX - shackleRadius, shackleCenterY - shackleRadius)
            cubicTo(
                shackleCenterX - shackleRadius, shackleCenterY - shackleRadius * 1.8f,
                shackleCenterX + shackleRadius, shackleCenterY - shackleRadius * 1.8f,
                shackleCenterX + shackleRadius, shackleCenterY - shackleRadius
            )
            lineTo(shackleCenterX + shackleRadius, shackleCenterY)
        }
        drawPath(
            path = shacklePath,
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFCBD5E1), Color(0xFFFFFFFF), Color(0xFF94A3B8))
            ),
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Padlock Body 3D Drop Shadow
        drawRoundRect(
            color = Color(0xFF78350F).copy(alpha = 0.45f),
            topLeft = Offset(bodyL, bodyT + 2.dp.toPx()),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
        )

        // Padlock Brass Body
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFDE7),
                    Color(0xFFFFD54F),
                    Color(0xFFF59E0B),
                    Color(0xFFB45309)
                ),
                center = Offset(w * 0.42f, bodyT + bodyH * 0.35f),
                radius = bodyW * 0.9f
            ),
            topLeft = Offset(bodyL, bodyT),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFFFFF9DB),
            topLeft = Offset(bodyL, bodyT),
            size = Size(bodyW, bodyH),
            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )

        // Keyhole (Center)
        val keyCenter = Offset(w / 2f, bodyT + bodyH * 0.42f)
        drawCircle(color = Color(0xFF451A03), radius = 2.2.dp.toPx(), center = keyCenter)
        val keySlot = Path().apply {
            moveTo(keyCenter.x - 1.2.dp.toPx(), keyCenter.y)
            lineTo(keyCenter.x - 1.8.dp.toPx(), keyCenter.y + 4.dp.toPx())
            lineTo(keyCenter.x + 1.8.dp.toPx(), keyCenter.y + 4.dp.toPx())
            lineTo(keyCenter.x + 1.2.dp.toPx(), keyCenter.y)
            close()
        }
        drawPath(path = keySlot, color = Color(0xFF451A03))
    }
}

/* =========================================================================
   6. 3D CANDY CITADEL (SUMMIT LANDMARK - Zero Emojis)
   ========================================================================= */

@Composable
fun CanvasCandyCitadel(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val cx = w / 2f
        val baseY = h * 0.88f

        // 1. Foundation Hill / Cake Base
        drawOval(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFDE047), Color(0xFFEAB308), Color(0xFFCA8A04))
            ),
            topLeft = Offset(w * 0.08f, baseY - 8.dp.toPx()),
            size = Size(w * 0.84f, 18.dp.toPx())
        )

        // 2. Central Sugar Palace Tower
        val mainTowerL = w * 0.34f
        val mainTowerR = w * 0.66f
        val mainTowerT = h * 0.38f
        val mainTowerW = mainTowerR - mainTowerL
        val mainTowerH = baseY - mainTowerT

        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFFF1F2), Color(0xFFFFD1DC), Color(0xFFFFAAA6))
            ),
            topLeft = Offset(mainTowerL, mainTowerT),
            size = Size(mainTowerW, mainTowerH),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Center Peppermint Portal Door
        val doorW = mainTowerW * 0.42f
        val doorH = mainTowerH * 0.45f
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFF4D6D), Color(0xFFC9184A), Color(0xFF800F2F))
            ),
            topLeft = Offset(cx - doorW / 2f, baseY - doorH),
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(doorW / 2f, doorW / 2f)
        )

        // 3. Central Waffle Spire Cone
        val spirePath = Path().apply {
            moveTo(mainTowerL, mainTowerT)
            lineTo(cx, h * 0.12f)
            lineTo(mainTowerR, mainTowerT)
            close()
        }
        drawPath(
            path = spirePath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFFF3B0), Color(0xFFFFB703), Color(0xFFD97706))
            )
        )
        // Waffle cross-hatch texture on spire
        drawLine(
            color = Color(0xFFB45309).copy(alpha = 0.5f),
            start = Offset(cx - mainTowerW * 0.25f, mainTowerT - 4.dp.toPx()),
            end = Offset(cx + mainTowerW * 0.25f, mainTowerT - 12.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )

        // Royal Pennant Flag on Top Spire
        val flagPath = Path().apply {
            moveTo(cx, h * 0.12f)
            lineTo(cx + 12.dp.toPx(), h * 0.16f)
            lineTo(cx, h * 0.20f)
            close()
        }
        drawPath(path = flagPath, color = Color(0xFFE11D48))
        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(cx, h * 0.08f),
            end = Offset(cx, h * 0.22f),
            strokeWidth = 1.5.dp.toPx()
        )

        // 4. Left & Right Turret Towers
        val turretW = w * 0.18f
        val turretH = h * 0.38f

        // Left Turret
        val leftTurretL = w * 0.16f
        val leftTurretT = h * 0.48f
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF7DD3FC))
            ),
            topLeft = Offset(leftTurretL, leftTurretT),
            size = Size(turretW, turretH),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
        val leftSpire = Path().apply {
            moveTo(leftTurretL, leftTurretT)
            lineTo(leftTurretL + turretW / 2f, leftTurretT - 16.dp.toPx())
            lineTo(leftTurretL + turretW, leftTurretT)
            close()
        }
        drawPath(path = leftSpire, color = Color(0xFF0284C7))

        // Right Turret
        val rightTurretL = w * 0.66f
        val rightTurretT = h * 0.48f
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF7DD3FC))
            ),
            topLeft = Offset(rightTurretL, rightTurretT),
            size = Size(turretW, turretH),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )
        val rightSpire = Path().apply {
            moveTo(rightTurretL, rightTurretT)
            lineTo(rightTurretL + turretW / 2f, rightTurretT - 16.dp.toPx())
            lineTo(rightTurretL + turretW, rightTurretT)
            close()
        }
        drawPath(path = rightSpire, color = Color(0xFF0284C7))
    }
}

/* =========================================================================
   7. ROADSIDE 3D CANDY PROPS (Zero Emojis)
   ========================================================================= */

@Composable
fun CanvasRoadsideProp(
    propType: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (propType % 5) {
            0 -> drawRainbowLollipop(cx, cy, w, h)
            1 -> drawSugarBlossom(cx, cy, w, h)
            2 -> drawWrappedBonbon(cx, cy, w, h)
            3 -> drawGummyMushroom(cx, cy, w, h)
            4 -> drawDiamondSparkleCluster(cx, cy, w, h)
        }
    }
}

/** 3D Rainbow Swirl Lollipop on stick */
private fun DrawScope.drawRainbowLollipop(cx: Float, cy: Float, w: Float, h: Float) {
    val stickW = 3.dp.toPx()
    val stickH = h * 0.50f
    val candyR = w * 0.32f
    val candyY = cy - h * 0.12f

    // White candy stick
    drawRoundRect(
        color = Color(0xFFF1F5F9),
        topLeft = Offset(cx - stickW / 2f, candyY),
        size = Size(stickW, stickH),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )

    // Candy drop shadow
    drawCircle(
        color = Color(0xFF6D3804).copy(alpha = 0.30f),
        radius = candyR,
        center = Offset(cx, candyY + 2.dp.toPx())
    )

    // Swirl Disc
    drawCircle(
        brush = Brush.sweepGradient(
            listOf(
                Color(0xFFFF5D8F),
                Color(0xFFFFD166),
                Color(0xFF06D6A0),
                Color(0xFF118AB2),
                Color(0xFFFF5D8F)
            ),
            center = Offset(cx, candyY)
        ),
        radius = candyR,
        center = Offset(cx, candyY)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.40f),
        radius = candyR * 0.45f,
        center = Offset(cx, candyY)
    )
    // White rim
    drawCircle(
        color = Color.White.copy(alpha = 0.80f),
        radius = candyR,
        center = Offset(cx, candyY),
        style = Stroke(width = 1.2.dp.toPx())
    )
    // Specular highlight
    drawOval(
        brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.75f), Color.Transparent)
        ),
        topLeft = Offset(cx - candyR * 0.6f, candyY - candyR * 0.8f),
        size = Size(candyR * 1.2f, candyR * 0.6f)
    )
}

/** 3D Sugar Blossom Flower with golden jewel core */
private fun DrawScope.drawSugarBlossom(cx: Float, cy: Float, w: Float, h: Float) {
    val petalR = w * 0.16f
    val dist = w * 0.22f

    for (i in 0 until 5) {
        val angle = i * (PI * 2 / 5)
        val px = (cx + dist * cos(angle)).toFloat()
        val py = (cy + dist * sin(angle)).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFF0F3), Color(0xFFFFCCD5), Color(0xFFFF758F)),
                center = Offset(px, py)
            ),
            radius = petalR,
            center = Offset(px, py)
        )
    }

    // Golden jewel core
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F), Color(0xFFD97706)),
            center = Offset(cx, cy)
        ),
        radius = w * 0.14f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color.White,
        radius = 1.2.dp.toPx(),
        center = Offset(cx - 1.dp.toPx(), cy - 1.dp.toPx())
    )
}

/** 3D Wrapped Candy Bonbon */
private fun DrawScope.drawWrappedBonbon(cx: Float, cy: Float, w: Float, h: Float) {
    val discR = w * 0.24f

    // Flap wings
    val leftFlap = Path().apply {
        moveTo(cx - discR * 0.7f, cy)
        lineTo(w * 0.08f, cy - discR * 0.8f)
        lineTo(w * 0.12f, cy)
        lineTo(w * 0.08f, cy + discR * 0.8f)
        close()
    }
    val rightFlap = Path().apply {
        moveTo(cx + discR * 0.7f, cy)
        lineTo(w * 0.92f, cy - discR * 0.8f)
        lineTo(w * 0.88f, cy)
        lineTo(w * 0.92f, cy + discR * 0.8f)
        close()
    }
    drawPath(path = leftFlap, color = Color(0xFF38BDF8).copy(alpha = 0.60f))
    drawPath(path = rightFlap, color = Color(0xFF38BDF8).copy(alpha = 0.60f))

    // Center disc
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7)),
            center = Offset(cx - discR * 0.3f, cy - discR * 0.3f)
        ),
        radius = discR,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = discR * 0.35f,
        center = Offset(cx - discR * 0.3f, cy - discR * 0.3f)
    )
}

/** 3D Gummy Mushroom / Gumdrop with white sugar dots */
private fun DrawScope.drawGummyMushroom(cx: Float, cy: Float, w: Float, h: Float) {
    val stemW = w * 0.22f
    val stemH = h * 0.38f
    val capR = w * 0.38f
    val capY = cy - h * 0.04f

    // Stem
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFFBEA), Color(0xFFF3E5D0))
        ),
        topLeft = Offset(cx - stemW / 2f, capY),
        size = Size(stemW, stemH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    // Red Gummy Cap
    val capPath = Path().apply {
        moveTo(cx - capR, capY)
        cubicTo(cx - capR, capY - capR * 1.3f, cx + capR, capY - capR * 1.3f, cx + capR, capY)
        close()
    }
    drawPath(
        path = capPath,
        brush = Brush.radialGradient(
            listOf(Color(0xFFFF758F), Color(0xFFE63946), Color(0xFF9B0024)),
            center = Offset(cx - capR * 0.3f, capY - capR * 0.5f)
        )
    )

    // Sugar Dots on Cap
    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 2.dp.toPx(), center = Offset(cx, capY - capR * 0.65f))
    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 1.6.dp.toPx(), center = Offset(cx - capR * 0.45f, capY - capR * 0.35f))
    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 1.6.dp.toPx(), center = Offset(cx + capR * 0.45f, capY - capR * 0.35f))
}

/** 3D Diamond Star Sparkle Cluster */
private fun DrawScope.drawDiamondSparkleCluster(cx: Float, cy: Float, w: Float, h: Float) {
    val r = w * 0.36f

    fun draw4PointStar(scx: Float, scy: Float, sr: Float, color: Color) {
        val path = Path().apply {
            moveTo(scx, scy - sr)
            lineTo(scx + sr * 0.25f, scy - sr * 0.25f)
            lineTo(scx + sr, scy)
            lineTo(scx + sr * 0.25f, scy + sr * 0.25f)
            lineTo(scx, scy + sr)
            lineTo(scx - sr * 0.25f, scy + sr * 0.25f)
            lineTo(scx - sr, scy)
            lineTo(scx - sr * 0.25f, scy - sr * 0.25f)
            close()
        }
        drawPath(path = path, color = color)
        drawCircle(color = Color.White, radius = sr * 0.20f, center = Offset(scx, scy))
    }

    // Hero Gold Star
    draw4PointStar(cx, cy, r, Color(0xFFFFD54F))
    // Side Satellite Star
    draw4PointStar(cx + r * 0.70f, cy - r * 0.60f, r * 0.45f, Color(0xFFFFFDE7))
}
