package com.squishout.engine.model

enum class DifficultyTier(val label: String, val badgeIcon: String) {
    BREATHER("Sweet Flow", "✨"),
    NORMAL("Normal", "🌸"),
    HARD("Hard Level", "🔥"),
    SUPER_HARD("Super Hard Level", "👑");

    val isChallenging: Boolean
        get() = this == HARD || this == SUPER_HARD

    companion object {
        fun forStage(stage: Int): DifficultyTier {
            if (stage <= 2) return NORMAL
            return when {
                stage % 10 == 0 -> SUPER_HARD
                stage % 10 == 5 -> HARD
                stage % 10 == 1 || stage % 10 == 6 -> BREATHER
                else -> NORMAL
            }
        }
    }
}
