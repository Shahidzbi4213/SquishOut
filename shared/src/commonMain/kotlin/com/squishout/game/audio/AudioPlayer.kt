package com.squishout.game.audio

enum class SoundEffect {
    POP,       // Crisp bubble pop (ploink!) on unblocked launch
    WOBBLE,    // Soft refusal bump on blocked tap
    VICTORY,   // Ascending chord fanfare on stage cleared
    BOOSTER,   // Sparkle shimmer on Hint/Undo/Wand
    CRACK,     // Crisp shattering sound on obstacle damage/break
    COMBO      // Sparkling musical arpeggio for rapid combo streak
}

enum class HapticFeedbackType {
    LIGHT_CLICK,       // Standard unblock launch or dock button tap
    ERROR_WOBBLE,      // Refusal bump on blocked tap
    CRACK_THUMP,       // Double pulse impact when obstacles crack or shatter
    VICTORY_FANFARE    // Ascending triplet vibration on stage clear
}

expect class AudioPlayer {
    fun playSound(sound: SoundEffect)
    fun triggerHaptic(type: HapticFeedbackType)
    fun triggerHaptic(isError: Boolean = false)
    fun release()
}

