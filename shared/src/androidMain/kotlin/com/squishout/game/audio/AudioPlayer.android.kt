package com.squishout.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual class AudioPlayer(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val tracks = mutableMapOf<SoundEffect, AudioTrack>()

    init {
        try {
            tracks[SoundEffect.POP] = createTrack(generatePop())
            tracks[SoundEffect.WOBBLE] = createTrack(generateWobble())
            tracks[SoundEffect.VICTORY] = createTrack(generateVictory())
            tracks[SoundEffect.BOOSTER] = createTrack(generateBooster())
            tracks[SoundEffect.CRACK] = createTrack(generateCrack())
            tracks[SoundEffect.COMBO] = createTrack(generateCombo())
        } catch (_: Throwable) {
            // AudioTrack creation fallback
        }
    }

    private fun createTrack(pcmData: ShortArray): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            )
            .setAudioFormat(
                android.media.AudioFormat.Builder()
                    .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcmData.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcmData, 0, pcmData.size)
        return track
    }

    private fun generatePop(): ShortArray {
        val sampleRate = 44100
        val durationSec = 0.065 // 65ms
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec
            // Sweep frequency from 750Hz down to 240Hz
            val freq = 750.0 - (progress * 510.0)
            phase += 2.0 * kotlin.math.PI * freq / sampleRate
            // Exponential volume decay envelope
            val envelope = kotlin.math.exp(-t * 45.0)
            val sample = (kotlin.math.sin(phase) * envelope * 24000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun generateWobble(): ShortArray {
        val sampleRate = 44100
        val durationSec = 0.12 // 120ms
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 120.0 - (t * 200.0)
            phase += 2.0 * kotlin.math.PI * freq / sampleRate
            val envelope = (1.0 - (t / durationSec)) * 0.85
            val sample = (kotlin.math.sin(phase) * envelope * 22000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun generateVictory(): ShortArray {
        val sampleRate = 44100
        val noteCount = 4
        val noteDur = 0.09 // 90ms each
        val totalSamples = (sampleRate * noteDur * noteCount).toInt()
        val buffer = ShortArray(totalSamples)
        val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val noteIdx = (t / noteDur).toInt().coerceIn(0, 3)
            val noteT = t - (noteIdx * noteDur)
            val freq = freqs[noteIdx]
            phase += 2.0 * kotlin.math.PI * freq / sampleRate
            val envelope = kotlin.math.exp(-noteT * 20.0)
            val sample = (kotlin.math.sin(phase) * envelope * 22000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun generateBooster(): ShortArray {
        val sampleRate = 44100
        val durationSec = 0.15 // 150ms
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec
            // Sweep up 880Hz to 1760Hz
            val freq = 880.0 + (progress * 880.0)
            phase += 2.0 * kotlin.math.PI * freq / sampleRate
            // Shimmer amplitude modulation
            val shimmer = 0.8 + 0.2 * kotlin.math.sin(2.0 * kotlin.math.PI * 18.0 * t)
            val envelope = (1.0 - progress) * shimmer
            val sample = (kotlin.math.sin(phase) * envelope * 20000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun generateCrack(): ShortArray {
        val sampleRate = 44100
        val durationSec = 0.08 // 80ms crisp crunch / fracture
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec
            // Dual frequency sweep: sharp high transient (1600Hz -> 300Hz) and body (600Hz -> 80Hz)
            val freq1 = 1600.0 * (1.0 - progress * 0.8)
            val freq2 = 600.0 * (1.0 - progress * 0.85)
            phase1 += 2.0 * kotlin.math.PI * freq1 / sampleRate
            phase2 += 2.0 * kotlin.math.PI * freq2 / sampleRate
            // Steep exponential decay for glass/ice fracture sound
            val envelope = kotlin.math.exp(-t * 55.0)
            val noise = (((i * 1103515245 + 12345) and 0x7FFF) / 32768.0 - 0.5) * 0.4
            val tone = kotlin.math.sin(phase1) * 0.65 + kotlin.math.sin(phase2) * 0.35
            val sample = ((tone + noise) * envelope * 24000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    private fun generateCombo(): ShortArray {
        val sampleRate = 44100
        val noteCount = 3
        val noteDur = 0.05 // 50ms each
        val totalSamples = (sampleRate * noteDur * noteCount).toInt()
        val buffer = ShortArray(totalSamples)
        val freqs = doubleArrayOf(880.0, 1100.0, 1320.0) // A5 -> C#6 -> E6 ascending arpeggio

        for (n in 0 until noteCount) {
            val startSample = (n * noteDur * sampleRate).toInt()
            val endSample = ((n + 1) * noteDur * sampleRate).toInt()
            var phase = 0.0
            val freq = freqs[n]
            for (i in startSample until endSample) {
                val t = (i - startSample).toDouble() / sampleRate
                phase += 2.0 * kotlin.math.PI * freq / sampleRate
                val envelope = kotlin.math.exp(-t * 35.0)
                val sample = (kotlin.math.sin(phase) * envelope * 24000.0).toInt().coerceIn(-32767, 32767)
                buffer[i] = sample.toShort()
            }
        }
        return buffer
    }

    actual fun playSound(sound: SoundEffect) {
        try {
            val track = tracks[sound] ?: return
            track.stop()
            track.reloadStaticData()
            track.play()
        } catch (_: Throwable) {
            // Safe fallback if audio device is unavailable
        }
    }

    actual fun triggerHaptic(type: HapticFeedbackType) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticFeedbackType.LIGHT_CLICK -> {
                        VibrationEffect.createOneShot(25, 100)
                    }
                    HapticFeedbackType.ERROR_WOBBLE -> {
                        VibrationEffect.createOneShot(80, 180)
                    }
                    HapticFeedbackType.CRACK_THUMP -> {
                        // Double pulse: 30ms pulse, 40ms pause, 50ms pulse
                        val timings = longArrayOf(0, 30, 40, 50)
                        val amplitudes = intArrayOf(0, 160, 0, 220)
                        VibrationEffect.createWaveform(timings, amplitudes, -1)
                    }
                    HapticFeedbackType.VICTORY_FANFARE -> {
                        // Ascending triplet fanfare: 40ms, 60ms pause, 40ms, 60ms pause, 80ms
                        val timings = longArrayOf(0, 40, 60, 40, 60, 80)
                        val amplitudes = intArrayOf(0, 120, 0, 180, 0, 255)
                        VibrationEffect.createWaveform(timings, amplitudes, -1)
                    }
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticFeedbackType.LIGHT_CLICK -> vibrator?.vibrate(25)
                    HapticFeedbackType.ERROR_WOBBLE -> vibrator?.vibrate(80)
                    HapticFeedbackType.CRACK_THUMP -> vibrator?.vibrate(longArrayOf(0, 30, 40, 50), -1)
                    HapticFeedbackType.VICTORY_FANFARE -> vibrator?.vibrate(longArrayOf(0, 40, 60, 40, 60, 80), -1)
                }
            }
        } catch (_: Throwable) {
            // Safe fallback if vibrator is unavailable or permission is denied
        }
    }

    actual fun triggerHaptic(isError: Boolean) {
        triggerHaptic(if (isError) HapticFeedbackType.ERROR_WOBBLE else HapticFeedbackType.LIGHT_CLICK)
    }

    actual fun release() {
        tracks.values.forEach { track ->
            try {
                track.stop()
                track.release()
            } catch (_: Throwable) {}
        }
        tracks.clear()
    }
}
