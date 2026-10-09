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
    private var isMusicEnabled = true
    private var isSoundEnabled = true
    private var isPausedByLifecycle = false

    actual fun playSound(sound: SoundEffect) {
        if (!isSoundEnabled || isPausedByLifecycle) return
        val soundId = when (sound) {
            SoundEffect.POP -> 1104u // Standard Apple pop sound
            SoundEffect.WOBBLE -> 1053u // Low tick
            SoundEffect.VICTORY -> 1025u // Fanfare chime
            SoundEffect.BOOSTER -> 1054u // Shimmer tick
            SoundEffect.CRACK -> 1052u // Shatter / crisp tick
            SoundEffect.COMBO -> 1105u // High pleasant pop
        }
        AudioServicesPlaySystemSound(soundId)
    }

    actual fun triggerHaptic(type: HapticFeedbackType) {
        if (isPausedByLifecycle) return
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

    actual fun startMusic() {
        if (!isMusicEnabled || isPausedByLifecycle) return
        // iOS background loop handler
    }

    actual fun stopMusic() {
        // iOS background loop handler
    }

    actual fun pauseMusic() {
        isPausedByLifecycle = true
        // iOS background loop handler
    }

    actual fun resumeMusic() {
        isPausedByLifecycle = false
        if (!isMusicEnabled) return
        // iOS background loop handler
    }

    actual fun pauseAll() {
        pauseMusic()
    }

    actual fun resumeAll() {
        isPausedByLifecycle = false
        resumeMusic()
    }

    actual fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (enabled) {
            if (!isPausedByLifecycle) startMusic()
        } else {
            stopMusic()
        }
    }

    actual fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    actual fun release() {
        isPausedByLifecycle = true
        stopMusic()
    }
}
