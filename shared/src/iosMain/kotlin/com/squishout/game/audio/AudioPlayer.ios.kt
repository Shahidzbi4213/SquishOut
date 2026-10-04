package com.squishout.game.audio

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual class AudioPlayer {

    private val impactLight = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    private val notificationFeedback = UINotificationFeedbackGenerator()

    actual fun playSound(sound: SoundEffect) {
        val soundId = when (sound) {
            SoundEffect.POP -> 1104u // Standard Apple pop sound
            SoundEffect.WOBBLE -> 1053u // Low tick
            SoundEffect.VICTORY -> 1025u // Fanfare chime
            SoundEffect.BOOSTER -> 1054u // Shimmer tick
        }
        AudioServicesPlaySystemSound(soundId)
    }

    actual fun triggerHaptic(isError: Boolean) {
        if (isError) {
            notificationFeedback.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
        } else {
            impactLight.impactOccurred()
        }
    }
}
