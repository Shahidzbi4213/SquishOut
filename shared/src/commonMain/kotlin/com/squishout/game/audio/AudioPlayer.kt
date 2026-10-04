package com.squishout.game.audio

enum class SoundEffect {
    POP,       // Crisp bubble pop (ploink!) on unblocked launch
    WOBBLE,    // Soft refusal bump on blocked tap
    VICTORY,   // Ascending chord fanfare on stage cleared
    BOOSTER    // Sparkle shimmer on Hint/Undo/Wand
}

expect class AudioPlayer {
    fun playSound(sound: SoundEffect)
    fun triggerHaptic(isError: Boolean = false)
}
