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

    actual fun triggerHaptic(isError: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (isError) {
                VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
            } else {
                VibrationEffect.createOneShot(25, 120) // Crisp pop click
            }
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(if (isError) 80 else 25)
        }
    }
}
