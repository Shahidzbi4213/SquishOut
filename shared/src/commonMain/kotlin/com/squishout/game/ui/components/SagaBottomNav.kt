package com.squishout.game.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.cos
import kotlin.math.sin

/**
 * Casual Game 3D Floating Arcade Navigation Dock ("The Gummy Harbor Dock").
 * Replaces generic flat tabs with an ultra-juicy, tactile 3D floating dock:
 * - Glazed porcelain ivory capsule with warm caramel bevel depth
 * - Dual decorative brass screw rivets at dock edges
 * - Recessed soft-cream well with inner shadow illusion
 * - Smooth spring-animated sliding golden-caramel active pill indicator
 * - Bespoke Canvas-crafted candy vector icons (Adventure Map, Squishy Jelly Paw, Peppermint Swirl)
 * - Tactile press-depression physics on touch
 * - Bouncy notification badge with ambient pulse
 */
@Composable
fun SagaBottomNav(
    selectedTab: String,
    onMapClick: () -> Unit,
    onDexClick: () -> Unit,
    onShopClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedIndex = when (selectedTab) {
        "map" -> 0
        "dex" -> 1
        "shop" -> 2
        else -> 0
    }

    val dockShape = RoundedCornerShape(36.dp)

    Box(
        modifier = modifier
            .widthIn(max = 370.dp)
            .padding(horizontal = 14.dp)
            // Layer 1: Warm ambient drop shadow
            .shadow(18.dp, dockShape, spotColor = Color(0xFF6D3804).copy(alpha = 0.45f))
            .clip(dockShape)
            // Layer 2: 3D bottom caramel/cedar wood rim bevel (depth = 4.5dp)
            .background(Color(0xFF8D4F0E))
            .padding(bottom = 4.5.dp)
    ) {
        // Layer 3: High-gloss glazed porcelain enamel well
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFDF9), // Specular ivory reflection
                            Color(0xFFFAF2E4), // Soft cream body
                            Color(0xFFF3E5D0)  // Warm honey undertone
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFFE8D1),
                            Color(0xFFE2C9A8)
                        )
                    ),
                    shape = RoundedCornerShape(34.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Decorative Left & Right Brass Rivets
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
            ) {
                DockBrassRivet()
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
            ) {
                DockBrassRivet()
            }

            // Interactive Tab Track with Sliding Active Gummy Pill
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                val totalWidth = maxWidth
                val tabWidth = totalWidth / 3

                // Spring physics sliding caramel pill
                val animatedIndex by animateFloatAsState(
                    targetValue = selectedIndex.toFloat(),
                    animationSpec = spring(
                        dampingRatio = 0.76f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "tab_pill_slider"
                )

                // 3D Sliding Caramel Indicator Pill
                Box(
                    modifier = Modifier
                        .offset(x = tabWidth * animatedIndex)
                        .width(tabWidth)
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val pillShape = RoundedCornerShape(22.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(54.dp)
                            // Warm amber glow under active tab
                            .shadow(8.dp, pillShape, spotColor = Color(0xFFD97706).copy(alpha = 0.60f))
                            .clip(pillShape)
                            // 3D extruded bottom amber bevel
                            .background(Color(0xFFB45309))
                            .padding(bottom = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFF9DB), // Glossy golden highlight
                                            Color(0xFFFFD54F), // Rich honeycomb gold
                                            Color(0xFFF59E0B)  // Warm amber core
                                        )
                                    )
                                )
                                .border(
                                    width = 1.2.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFFFFFBEA), Color(0xFFFFE082))
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            // Specular top gloss crescent
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawOval(
                                    brush = Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.70f), Color.Transparent)
                                    ),
                                    topLeft = Offset(size.width * 0.15f, 1.5f),
                                    size = Size(size.width * 0.70f, size.height * 0.32f)
                                )
                            }
                        }
                    }
                }

                // 3 Interactive Navigation Tabs Layer (Z-Indexed above slider)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // TAB 1: Adventure Map
                    SagaNavTab(
                        label = "Map",
                        isActive = selectedIndex == 0,
                        onClick = onMapClick,
                        iconContent = { isActive ->
                            NavMapIcon(isActive = isActive)
                        },
                        modifier = Modifier.width(tabWidth)
                    )

                    // TAB 2: Jellies Dex
                    SagaNavTab(
                        label = "Jellies",
                        isActive = selectedIndex == 1,
                        onClick = onDexClick,
                        badge = "8",
                        iconContent = { isActive ->
                            NavJelliesPawIcon(isActive = isActive)
                        },
                        modifier = Modifier.width(tabWidth)
                    )

                    // TAB 3: Shop
                    SagaNavTab(
                        label = "Shop",
                        isActive = selectedIndex == 2,
                        onClick = onShopClick,
                        iconContent = { isActive ->
                            NavShopDiamondIcon(isActive = isActive)
                        },
                        modifier = Modifier.width(tabWidth)
                    )
                }
            }
        }
    }
}

/**
 * Individual tab item with tactile press spring scale, custom icon slot, and arcade label.
 */
@Composable
private fun SagaNavTab(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    iconContent: @Composable (isActive: Boolean) -> Unit,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_tab_press_scale"
    )

    Box(
        modifier = modifier
            .height(62.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                // Icon Slot
                Box(
                    modifier = Modifier.size(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    iconContent(isActive)
                }

                // 3D Notification Badge (Tactile mint-red bubble)
                if (badge != null && !isActive) {
                    NavPulsingBadge(badge = badge)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Tab Text Label
            Text(
                text = label,
                fontSize = if (isActive) 11.5.sp else 10.5.sp,
                fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                color = if (isActive) Color(0xFF4E2600) else Color(0xFF7E7262),
                letterSpacing = 0.4.sp,
                style = TextStyle(
                    shadow = if (isActive) {
                        Shadow(
                            color = Color(0xFFFFECC0),
                            offset = Offset(0f, 1f),
                            blurRadius = 1f
                        )
                    } else null
                )
            )
        }
    }
}

/**
 * 3D Notification bubble with ambient breathing pulse.
 */
@Composable
private fun NavPulsingBadge(badge: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )

    Box(
        modifier = Modifier
            .offset(x = 6.dp, y = (-3).dp)
            .scale(pulseScale)
            .size(16.dp)
            .shadow(3.dp, CircleShape, spotColor = Color(0xFF047857))
            .clip(CircleShape)
            // 3D bottom bevel
            .background(Color(0xFF065F46))
            .padding(bottom = 1.5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669))
                    )
                )
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

/* =========================================================================
   BESPOKE CANVAS-CRAFTED CANDY VECTOR ICONS (Zero Flat Emojis)
   ========================================================================= */

/**
 * Adventure Map Icon:
 * Folded parchment scroll with rolled edges, turquoise winding path, and 4-point golden compass rose.
 */
@Composable
private fun NavMapIcon(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_idle")
    val idleTilt by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "map_tilt"
    )

    val scale = if (isActive) 1.08f else 0.95f

    Canvas(
        modifier = Modifier
            .size(28.dp)
            .scale(scale)
    ) {
        val w = size.width
        val h = size.height

        // 1. Folded Map Scroll Body (3-fold zigzag polygon)
        val mapPath = Path().apply {
            moveTo(w * 0.12f, h * 0.22f)
            lineTo(w * 0.38f, h * 0.14f)
            lineTo(w * 0.64f, h * 0.22f)
            lineTo(w * 0.88f, h * 0.14f)
            lineTo(w * 0.88f, h * 0.82f)
            lineTo(w * 0.64f, h * 0.90f)
            lineTo(w * 0.38f, h * 0.82f)
            lineTo(w * 0.12f, h * 0.90f)
            close()
        }

        // Map drop shadow
        drawPath(
            path = mapPath,
            color = if (isActive) Color(0xFFB45309).copy(alpha = 0.35f) else Color(0xFF9E8E79).copy(alpha = 0.20f)
        )

        // Parchment base gradient
        val parchmentBrush = if (isActive) {
            Brush.verticalGradient(
                listOf(Color(0xFFFFFBF0), Color(0xFFFBEBC8), Color(0xFFF0D59D))
            )
        } else {
            Brush.verticalGradient(
                listOf(Color(0xFFFAF7F0), Color(0xFFEDE4D2), Color(0xFFDDD2BC))
            )
        }
        drawPath(path = mapPath, brush = parchmentBrush)

        // Map borders and fold creases
        val strokeColor = if (isActive) Color(0xFF92400E) else Color(0xFF9E8E79)
        drawPath(
            path = mapPath,
            color = strokeColor,
            style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Fold crease 1
        drawLine(
            color = strokeColor.copy(alpha = 0.5f),
            start = Offset(w * 0.38f, h * 0.14f),
            end = Offset(w * 0.38f, h * 0.82f),
            strokeWidth = 1.dp.toPx()
        )
        // Fold crease 2
        drawLine(
            color = strokeColor.copy(alpha = 0.5f),
            start = Offset(w * 0.64f, h * 0.22f),
            end = Offset(w * 0.64f, h * 0.90f),
            strokeWidth = 1.dp.toPx()
        )

        // 2. Turquoise Winding Adventure Path
        val pathDash = Path().apply {
            moveTo(w * 0.20f, h * 0.72f)
            cubicTo(w * 0.35f, h * 0.55f, w * 0.45f, h * 0.65f, w * 0.60f, h * 0.45f)
            lineTo(w * 0.76f, h * 0.35f)
        }
        drawPath(
            path = pathDash,
            color = if (isActive) Color(0xFF0D9488) else Color(0xFF64748B),
            style = Stroke(
                width = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // 3. Compass Star / Navigation Pin
        val pinCenter = Offset(w * 0.72f, h * 0.36f)
        val pinRadius = 4.5.dp.toPx()

        // Golden 4-point star points
        val starPath = Path().apply {
            moveTo(pinCenter.x, pinCenter.y - pinRadius * 1.6f)
            lineTo(pinCenter.x + pinRadius * 0.45f, pinCenter.y - pinRadius * 0.45f)
            lineTo(pinCenter.x + pinRadius * 1.6f, pinCenter.y)
            lineTo(pinCenter.x + pinRadius * 0.45f, pinCenter.y + pinRadius * 0.45f)
            lineTo(pinCenter.x, pinCenter.y + pinRadius * 1.6f)
            lineTo(pinCenter.x - pinRadius * 0.45f, pinCenter.y + pinRadius * 0.45f)
            lineTo(pinCenter.x - pinRadius * 1.6f, pinCenter.y)
            lineTo(pinCenter.x - pinRadius * 0.45f, pinCenter.y - pinRadius * 0.45f)
            close()
        }
        drawPath(
            path = starPath,
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F), Color(0xFFD97706)),
                center = pinCenter
            )
        )
        // Red gem center in compass
        drawCircle(
            color = if (isActive) Color(0xFFE11D48) else Color(0xFF94A3B8),
            radius = 1.6.dp.toPx(),
            center = pinCenter
        )
    }
}

/**
 * Jellies Dex Icon:
 * Juicy 3D Strawberry Jelly Paw with glossy toe-beans and specular crescent highlights.
 */
@Composable
private fun NavJelliesPawIcon(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "paw_breathe")
    val idleSquish by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "paw_squish"
    )

    val scale = if (isActive) idleSquish else 0.95f

    Canvas(
        modifier = Modifier
            .size(28.dp)
            .scale(scale)
    ) {
        val w = size.width
        val h = size.height

        val padColor1 = if (isActive) Color(0xFFFF4D6D) else Color(0xFFF472B6)
        val padColor2 = if (isActive) Color(0xFFC9184A) else Color(0xFFDB2777)
        val shadowColor = if (isActive) Color(0xFF800F2F) else Color(0xFF9D174D)

        // 1. Center Main Jelly Pad (Squishy heart/trapezoid bean)
        val padCenter = Offset(w * 0.50f, h * 0.64f)
        val padRadiusX = w * 0.32f
        val padRadiusY = h * 0.24f

        // Pad 3D drop shadow
        drawOval(
            color = shadowColor,
            topLeft = Offset(padCenter.x - padRadiusX, padCenter.y - padRadiusY + 2.dp.toPx()),
            size = Size(padRadiusX * 2f, padRadiusY * 2f)
        )

        // Pad body
        drawOval(
            brush = Brush.verticalGradient(
                listOf(padColor1, padColor2),
                startY = padCenter.y - padRadiusY,
                endY = padCenter.y + padRadiusY
            ),
            topLeft = Offset(padCenter.x - padRadiusX, padCenter.y - padRadiusY),
            size = Size(padRadiusX * 2f, padRadiusY * 2f)
        )

        // Pad white specular crescent highlight
        drawOval(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.75f), Color.Transparent)
            ),
            topLeft = Offset(padCenter.x - padRadiusX * 0.65f, padCenter.y - padRadiusY * 0.85f),
            size = Size(padRadiusX * 1.3f, padRadiusY * 0.65f)
        )

        // 2. Three Toe Beans Arched Above Main Pad
        val toeBeans = listOf(
            Triple(w * 0.24f, h * 0.32f, 4.2.dp.toPx()), // Left toe
            Triple(w * 0.50f, h * 0.22f, 4.8.dp.toPx()), // Middle top toe
            Triple(w * 0.76f, h * 0.32f, 4.2.dp.toPx())  // Right toe
        )

        for ((tx, ty, tr) in toeBeans) {
            // Toe bottom shadow
            drawCircle(
                color = shadowColor,
                radius = tr,
                center = Offset(tx, ty + 1.2.dp.toPx())
            )

            // Toe gradient
            drawCircle(
                brush = Brush.verticalGradient(
                    listOf(padColor1, padColor2),
                    startY = ty - tr,
                    endY = ty + tr
                ),
                radius = tr,
                center = Offset(tx, ty)
            )

            // Toe specular gloss dot
            drawCircle(
                color = Color.White.copy(alpha = 0.80f),
                radius = tr * 0.35f,
                center = Offset(tx - tr * 0.25f, ty - tr * 0.25f)
            )
        }
    }
}

/**
 * Diamond Shop Icon:
 * 3D Shiny Faceted Diamond Vector Icon with idle shimmer animation.
 */
@Composable
private fun NavShopDiamondIcon(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "diamond_shimmer")
    val idleScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "diamond_pulse"
    )

    val scale = (if (isActive) 1.10f else 0.95f) * idleScale

    Canvas(
        modifier = Modifier
            .size(28.dp)
            .scale(scale)
    ) {
        val w = size.width
        val h = size.height

        // 1. Soft azure ambient drop shadow
        val shadowPath = Path().apply {
            moveTo(w * 0.25f, h * 0.22f + 2.dp.toPx())
            lineTo(w * 0.75f, h * 0.22f + 2.dp.toPx())
            lineTo(w * 0.95f, h * 0.48f + 2.dp.toPx())
            lineTo(w * 0.50f, h * 0.92f + 2.dp.toPx())
            lineTo(w * 0.05f, h * 0.48f + 2.dp.toPx())
            close()
        }
        drawPath(
            path = shadowPath,
            color = Color(0xFF0369A1).copy(alpha = 0.35f)
        )

        // 2. Base Brilliant Faceted Diamond Polygon
        val mainPath = Path().apply {
            moveTo(w * 0.25f, h * 0.20f)
            lineTo(w * 0.75f, h * 0.20f)
            lineTo(w * 0.95f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.90f)
            lineTo(w * 0.05f, h * 0.46f)
            close()
        }
        drawPath(
            path = mainPath,
            brush = Brush.verticalGradient(
                colors = if (isActive) {
                    listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                } else {
                    listOf(Color(0xFFF1F5F9), Color(0xFF94A3B8), Color(0xFF64748B))
                }
            ),
            style = Fill
        )

        // 3. Facet lines & upper table facet
        val tablePath = Path().apply {
            moveTo(w * 0.34f, h * 0.20f)
            lineTo(w * 0.66f, h * 0.20f)
            lineTo(w * 0.50f, h * 0.46f)
            close()
        }
        drawPath(
            path = tablePath,
            color = Color.White.copy(alpha = if (isActive) 0.85f else 0.50f),
            style = Fill
        )

        // Left upper facet
        val leftFacet = Path().apply {
            moveTo(w * 0.25f, h * 0.20f)
            lineTo(w * 0.34f, h * 0.20f)
            lineTo(w * 0.50f, h * 0.46f)
            lineTo(w * 0.05f, h * 0.46f)
            close()
        }
        drawPath(
            path = leftFacet,
            color = Color.White.copy(alpha = if (isActive) 0.50f else 0.30f),
            style = Fill
        )

        // Right upper facet
        val rightFacet = Path().apply {
            moveTo(w * 0.75f, h * 0.20f)
            lineTo(w * 0.66f, h * 0.20f)
            lineTo(w * 0.50f, h * 0.46f)
            lineTo(w * 0.95f, h * 0.46f)
            close()
        }
        drawPath(
            path = rightFacet,
            color = Color.White.copy(alpha = if (isActive) 0.35f else 0.20f),
            style = Fill
        )

        // Pavilion center dividing facet line
        val centerV = Path().apply {
            moveTo(w * 0.50f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.90f)
        }
        drawPath(
            path = centerV,
            color = Color.White.copy(alpha = 0.70f),
            style = Stroke(width = 1.2.dp.toPx())
        )

        // Outline contour
        drawPath(
            path = mainPath,
            color = if (isActive) Color(0xFFBAE6FD) else Color(0xFFCBD5E1),
            style = Stroke(width = 1.2.dp.toPx())
        )

        // Top specular sparkle dot
        drawCircle(
            color = Color.White,
            radius = 1.8.dp.toPx(),
            center = Offset(w * 0.32f, h * 0.24f)
        )
    }
}

/**
 * Miniature decorative 3D brass screw rivet for dock corners.
 */
@Composable
private fun DockBrassRivet() {
    Canvas(modifier = Modifier.size(6.dp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer bronze rim
        drawCircle(
            color = Color(0xFF78350F),
            radius = radius,
            center = center
        )

        // Golden center dome
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFDE7),
                    Color(0xFFFFD54F),
                    Color(0xFFD97706)
                ),
                center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
                radius = radius * 0.8f
            ),
            radius = radius * 0.8f,
            center = center
        )
    }
}
