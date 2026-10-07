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
            subtitle = "Stages 1 - 25",
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
            subtitle = "Stages 26 - 50",
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
            subtitle = "Stages 51 - 75",
            icon = "🍯",
            backgroundTop = Color(0xFFFFFBEB),
            backgroundBottom = Color(0xFFFDE68A),
            trayRimLight = Color(0xFFFEF3C7),
            trayRimDark = Color(0xFFFCD34D),
            trayFloor = Color(0xFFFFFBEB),
            accentPill = Color(0xFFD97706),
            accentText = Color(0xFF92400E)
        )

        val COTTON_CANDY_PEAK = BiomeTheme(
            id = "cotton_candy_peak",
            name = "Cotton Candy Peak",
            subtitle = "Stages 76 - 100",
            icon = "☁️",
            backgroundTop = Color(0xFFFDF4FF),
            backgroundBottom = Color(0xFFF5D0FE),
            trayRimLight = Color(0xFFFAF5FF),
            trayRimDark = Color(0xFFE879F9),
            trayFloor = Color(0xFFFDF4FF),
            accentPill = Color(0xFFC026D3),
            accentText = Color(0xFF701A75)
        )

        val LICORICE_LABYRINTH = BiomeTheme(
            id = "licorice_labyrinth",
            name = "Licorice Labyrinth",
            subtitle = "Stages 101 - 125",
            icon = "🌌",
            backgroundTop = Color(0xFFF5F3FF),
            backgroundBottom = Color(0xFFDDD6FE),
            trayRimLight = Color(0xFFEDE9FE),
            trayRimDark = Color(0xFF8B5CF6),
            trayFloor = Color(0xFFF5F3FF),
            accentPill = Color(0xFF7C3AED),
            accentText = Color(0xFF4C1D95)
        )

        val STARLIGHT_KINGDOM = BiomeTheme(
            id = "starlight_kingdom",
            name = "Starlight Kingdom",
            subtitle = "Stages 126 - 150",
            icon = "✨",
            backgroundTop = Color(0xFFFEFCE8),
            backgroundBottom = Color(0xFFFEF08A),
            trayRimLight = Color(0xFFFEF9C3),
            trayRimDark = Color(0xFFEAB308),
            trayFloor = Color(0xFFFEFCE8),
            accentPill = Color(0xFFCA8A04),
            accentText = Color(0xFF713F12)
        )

        fun forStage(stage: Int): BiomeTheme = when {
            stage <= 25 -> SWEET_MEADOW
            stage <= 50 -> SODA_LAGOON
            stage <= 75 -> HONEYCOMB_VALLEY
            stage <= 100 -> COTTON_CANDY_PEAK
            stage <= 125 -> LICORICE_LABYRINTH
            else -> STARLIGHT_KINGDOM
        }
    }
}
