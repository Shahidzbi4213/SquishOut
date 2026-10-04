package com.squishout.game.audio

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual class AudioPlayer {

    private val impactLight = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    private val impactHeavy = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
    private val notificationFeedback = UINotificationFeedbackGenerator()

    actual fun playSound(sound: SoundEffect) {
        val soundId = when (sound) {
            SoundEffect.POP -> 1104u // Standard Apple pop sound
            SoundEffect.WOBBLE -> 1053u // Low tick
            SoundEffect.VICTORY -> 1025u // Fanfare chime
            SoundEffect.BOOSTER -> 1054u // Shimmer tick
            SoundEffect.CRACK -> 1052u // Shatter / crisp tick
        }
        AudioServicesPlaySystemSound(soundId)
    }

    actual fun triggerHaptic(type: HapticFeedbackType) {
        when (type) {
            HapticFeedbackType.LIGHT_CLICK -> {
                impactLight.impactOccurred()
            }
            HapticFeedbackType.ERROR_WOBBLE -> {
                notificationFeedback.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
            }
            HapticFeedbackType.CRACK_THUMP -> {
                impactHeavy.impactOccurred()
            }
            HapticFeedbackType.VICTORY_FANFARE -> {
                notificationFeedback.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
            }
        }
    }

    actual fun triggerHaptic(isError: Boolean) {
        triggerHaptic(if (isError) HapticFeedbackType.ERROR_WOBBLE else HapticFeedbackType.LIGHT_CLICK)
    }
}
