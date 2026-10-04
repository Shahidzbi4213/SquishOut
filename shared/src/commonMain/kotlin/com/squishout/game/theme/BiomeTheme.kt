package com.squishout.game.theme

import androidx.compose.ui.graphics.Color

data class BiomeTheme(
    val id: String,
    val name: String,
    val subtitle: String,
    val icon: String,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val trayRimLight: Color,
    val trayRimDark: Color,
    val trayFloor: Color,
    val accentPill: Color,
    val accentText: Color
) {
    companion object {
        val SWEET_MEADOW = BiomeTheme(
            id = "sweet_meadow",
            name = "Sweet Meadow",
            subtitle = "Stages 1 - 20",
            icon = "🌸",
            backgroundTop = Color(0xFFF0FDF4),
            backgroundBottom = Color(0xFFDCFCE7),
            trayRimLight = Color(0xFFF1F5F9),
            trayRimDark = Color(0xFFCBD5E1),
            trayFloor = Color(0xFFF8FAFC),
            accentPill = Color(0xFF10B981),
            accentText = Color(0xFF065F46)
        )

        val SODA_LAGOON = BiomeTheme(
            id = "soda_lagoon",
            name = "Soda Lagoon",
            subtitle = "Stages 21 - 40",
            icon = "🌊",
            backgroundTop = Color(0xFFF0F9FF),
            backgroundBottom = Color(0xFFBAE6FD),
            trayRimLight = Color(0xFFE0F2FE),
            trayRimDark = Color(0xFF7DD3FC),
            trayFloor = Color(0xFFF0F9FF),
            accentPill = Color(0xFF0284C7),
            accentText = Color(0xFF0369A1)
        )

        val HONEYCOMB_VALLEY = BiomeTheme(
            id = "honeycomb_valley",
            name = "Honeycomb Valley",
            subtitle = "Stages 41+",
            icon = "🍯",
            backgroundTop = Color(0xFFFFFBEB),
            backgroundBottom = Color(0xFFFDE68A),
            trayRimLight = Color(0xFFFEF3C7),
            trayRimDark = Color(0xFFFCD34D),
            trayFloor = Color(0xFFFFFBEB),
            accentPill = Color(0xFFD97706),
            accentText = Color(0xFF92400E)
        )

        fun forStage(stage: Int): BiomeTheme = when {
            stage <= 20 -> SWEET_MEADOW
            stage <= 40 -> SODA_LAGOON
            else -> HONEYCOMB_VALLEY
        }
    }
}
