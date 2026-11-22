package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.AudioSettings
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural stereo ambient audio engine built on Android AudioTrack (44.1kHz 16-bit PCM).
 *
 * Generates:
 * 1) Seamless, non-repetitive warm felt-piano + ambient pad + woodland breeze background music.
 * 2) Polyphonic delicate sound effects (pentatonic dew chimes, lantern bloom chords,
 *    foliage rustle, gentle wooden UI clicks, and spirit flute tones).
 *
 * Supports independent Music and SFX volume/mute controls and clean lifecycle pause/resume.
 */
class AmbientAudioEngine {

    enum class SfxType {
        DEW_CHIME,
        LANTERN_BLOOM,
        TREE_RUSTLE,
        THREAD_SHIMMER,
        UI_TAP,
        SPIRIT_COO
    }

    private data class SfxVoice(
        val type: SfxType,
        val freqHz: Double,
        val secondaryFreqHz: Double,
        val durationSec: Double,
        var ageSec: Double = 0.0,
        val pan: Double = 0.0
    )

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val TWO_PI = 2.0 * PI

        // F Lydian / Major Pentatonic soothing frequencies (Hz)
        private val DEW_SCALE_HZ = doubleArrayOf(
            523.25, // C5
            587.33, // D5
            659.25, // E5
            783.99, // G5
            880.00, // A5
            1046.50 // C6
        )

        // Warm 4-chord ambient progression: Fmaj7, G6, Em7, Am9
        private val PAD_CHORDS = arrayOf(
            doubleArrayOf(174.61, 220.00, 261.63, 329.63), // Fmaj7
            doubleArrayOf(196.00, 246.94, 293.66, 329.63), // G6
            doubleArrayOf(164.81, 196.00, 246.94, 293.66), // Em7
            doubleArrayOf(220.00, 261.63, 329.63, 392.00)  // Am7
        )

        private val PIANO_ARPEGGIO = doubleArrayOf(
            349.23, 440.00, 523.25, 659.25,
            587.33, 493.88, 392.00, 523.25,
            329.63, 392.00, 493.88, 659.25,
            440.00, 523.25, 659.25, 783.99
        )
    }

    @Volatile
    private var settings = AudioSettings()

    @Volatile
    private var isPaused = false

    @Volatile
    private var isReleased = false

    private val pendingVoices = ConcurrentLinkedQueue<SfxVoice>()
    private val activeVoices = ArrayList<SfxVoice>(24)
    private var scaleStep = 0

    private var audioThread: Thread? = null
    private var audioTrack: AudioTrack? = null

    fun start() {
        if (isReleased || audioThread != null) return
        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(minBuf * 2)
                .build()
        }.getOrNull() ?: return

        if (track.state != AudioTrack.STATE_INITIALIZED) {
            track.release()
            return
        }

        audioTrack = track
        if (!isPaused) {
            runCatching { track.play() }
        }

        audioThread = Thread({ renderLoop(track) }, "WillowlightAudio").apply {
            priority = Thread.NORM_PRIORITY + 1
            isDaemon = true
            start()
        }
    }

    fun updateSettings(newSettings: AudioSettings) {
        settings = newSettings
    }

    fun pause() {
        isPaused = true
        runCatching { audioTrack?.pause() }
    }

    fun resume() {
        isPaused = false
        if (audioTrack == null && !isReleased) {
            start()
        } else {
            runCatching { audioTrack?.play() }
        }
    }

    fun release() {
        isReleased = true
        audioThread?.interrupt()
        audioThread = null
        runCatching {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        }
        audioTrack = null
    }

    fun playSfx(type: SfxType, pan: Float = 0f) {
        if (isPaused || settings.isSfxMuted || settings.sfxVolume <= 0.01f) return
        val clampedPan = pan.coerceIn(-0.7f, 0.7f).toDouble()
        val voice = when (type) {
            SfxType.DEW_CHIME -> {
                val hz = DEW_SCALE_HZ[scaleStep % DEW_SCALE_HZ.size]
                scaleStep++
                SfxVoice(type, hz, hz * 2.0, 0.85, pan = clampedPan)
            }
            SfxType.LANTERN_BLOOM -> {
                SfxVoice(type, 349.23, 523.25, 1.9, pan = clampedPan)
            }
            SfxType.TREE_RUSTLE -> {
                SfxVoice(type, 440.0, 587.33, 0.7, pan = clampedPan)
            }
            SfxType.THREAD_SHIMMER -> {
                SfxVoice(type, 659.25, 987.77, 0.35, pan = clampedPan)
            }
            SfxType.UI_TAP -> {
                SfxVoice(type, 392.0, 587.33, 0.14, pan = 0.0)
            }
            SfxType.SPIRIT_COO -> {
                SfxVoice(type, 523.25, 659.25, 0.75, pan = clampedPan)
            }
        }
        if (pendingVoices.size < 20) {
            pendingVoices.offer(voice)
        }
    }

    private fun renderLoop(track: AudioTrack) {
        val framesPerChunk = 1024
        val pcmBuffer = ShortArray(framesPerChunk * 2)
        val dt = 1.0 / SAMPLE_RATE

        // Delay line for warm stereo ambient tail (~320ms)
        val delayLen = (SAMPLE_RATE * 0.32).toInt()
        val delayL = DoubleArray(delayLen)
        val delayR = DoubleArray(delayLen)
        var delayIdx = 0

        // Smoothed gains to avoid clicks
        var smoothMusicGain = 0.0
        var smoothSfxGain = 0.0
        var smoothMasterGate = 0.0

        var musicTime = 0.0
        var pianoTimer = 0.4
        var pianoIdx = 0
        var activePianoFreq = 349.23
        var activePianoEnv = 0.0

        var pinkB0 = 0.0
        var pinkB1 = 0.0
        var pinkB2 = 0.0
        val rng = Random(42)

        while (!isReleased && !Thread.currentThread().isInterrupted) {
            if (isPaused) {
                smoothMasterGate = 0.0
                try {
                    Thread.sleep(40)
                } catch (_: InterruptedException) {
                    break
                }
                continue
            }

            while (true) {
                val next = pendingVoices.poll() ?: break
                if (activeVoices.size < 20) {
                    activeVoices.add(next)
                }
            }

            val currentSettings = settings
            val targetMusicGain = if (currentSettings.isMusicMuted) 0.0 else currentSettings.musicVolume.toDouble() * 0.48
            val targetSfxGain = if (currentSettings.isSfxMuted) 0.0 else currentSettings.sfxVolume.toDouble() * 0.55

            for (i in 0 until framesPerChunk) {
                smoothMusicGain += (targetMusicGain - smoothMusicGain) * 0.0015
                smoothSfxGain += (targetSfxGain - smoothSfxGain) * 0.004
                smoothMasterGate += (1.0 - smoothMasterGate) * 0.001

                // 1. Chord progression (6.5s per chord, smooth crossfade)
                val chordDuration = 6.5
                val totalProg = musicTime / chordDuration
                val chordIndex = totalProg.toInt() % PAD_CHORDS.size
                val nextChordIndex = (chordIndex + 1) % PAD_CHORDS.size
                val frac = totalProg - totalProg.toInt()
                val blend = if (frac > 0.72) {
                    val t = (frac - 0.72) / 0.28
                    0.5 - 0.5 * cos(PI * t)
                } else {
                    0.0
                }

                val cA = PAD_CHORDS[chordIndex]
                val cB = PAD_CHORDS[nextChordIndex]
                var padL = 0.0
                var padR = 0.0
                for (n in 0 until 4) {
                    val fA = cA[n]
                    val fB = cB[n]
                    val wA = (1.0 - blend) * 0.13
                    val wB = blend * 0.13
                    padL += wA * (sin(TWO_PI * (fA - 0.35) * musicTime) + 0.18 * sin(TWO_PI * fA * 2.0 * musicTime))
                    padR += wA * (sin(TWO_PI * (fA + 0.35) * musicTime) + 0.18 * sin(TWO_PI * fA * 2.0 * musicTime))
                    padL += wB * (sin(TWO_PI * (fB - 0.35) * musicTime) + 0.18 * sin(TWO_PI * fB * 2.0 * musicTime))
                    padR += wB * (sin(TWO_PI * (fB + 0.35) * musicTime) + 0.18 * sin(TWO_PI * fB * 2.0 * musicTime))
                }

                // 2. Soft felt-piano melody notes
                pianoTimer -= dt
                if (pianoTimer <= 0.0) {
                    activePianoFreq = PIANO_ARPEGGIO[pianoIdx % PIANO_ARPEGGIO.size]
                    pianoIdx++
                    activePianoEnv = 0.001
                    pianoTimer = if (pianoIdx % 4 == 0) 2.4 else 1.55
                }

                var pianoSample = 0.0
                if (activePianoEnv > 0.0) {
                    val age = (2.4 - pianoTimer).coerceAtLeast(0.0)
                    val attack = (age / 0.032).coerceIn(0.0, 1.0)
                    val decay = exp(-age * 1.45)
                    val env = attack * decay
                    pianoSample = env * 0.24 * (
                        sin(TWO_PI * activePianoFreq * musicTime) +
                            0.25 * sin(TWO_PI * activePianoFreq * 2.0 * musicTime) * exp(-age * 2.5)
                        )
                    if (decay < 0.001) activePianoEnv = 0.0
                }

                // 3. Soft filtered woodland breeze (ultra-subtle warm pink noise)
                val white = rng.nextDouble(-1.0, 1.0)
                pinkB0 = 0.997 * pinkB0 + white * 0.018
                pinkB1 = 0.985 * pinkB1 + white * 0.028
                pinkB2 = 0.950 * pinkB2 + white * 0.035
                val breezeMod = 0.55 + 0.45 * sin(TWO_PI * 0.09 * musicTime)
                val breeze = (pinkB0 + pinkB1 + pinkB2) * 0.028 * breezeMod

                // Gentle stereo reverb feedback for music
                val dryMusicL = (padL + pianoSample * 0.9 + breeze) * smoothMusicGain
                val dryMusicR = (padR + pianoSample * 1.05 + breeze) * smoothMusicGain
                val wetL = delayR[delayIdx] * 0.36
                val wetR = delayL[delayIdx] * 0.36
                delayL[delayIdx] = dryMusicL + wetL * 0.45
                delayR[delayIdx] = dryMusicR + wetR * 0.45

                // 4. Polyphonic SFX rendering
                var sfxL = 0.0
                var sfxR = 0.0
                var vIdx = activeVoices.size - 1
                while (vIdx >= 0) {
                    val v = activeVoices[vIdx]
                    v.ageSec += dt
                    if (v.ageSec >= v.durationSec) {
                        activeVoices.removeAt(vIdx)
                    } else {
                        val s = synthesizeVoiceSample(v, rng)
                        val leftGain = 0.5 * (1.0 - v.pan)
                        val rightGain = 0.5 * (1.0 + v.pan)
                        sfxL += s * leftGain * 1.4
                        sfxR += s * rightGain * 1.4
                    }
                    vIdx--
                }
                sfxL *= smoothSfxGain
                sfxR *= smoothSfxGain

                // Feed subtle SFX tail into delay reverb too
                delayL[delayIdx] += sfxL * 0.22
                delayR[delayIdx] += sfxR * 0.22
                delayIdx = (delayIdx + 1) % delayLen

                val mixL = ((dryMusicL + wetL + sfxL) * smoothMasterGate).coerceIn(-0.95, 0.95)
                val mixR = ((dryMusicR + wetR + sfxR) * smoothMasterGate).coerceIn(-0.95, 0.95)

                pcmBuffer[i * 2] = (mixL * 32767.0).toInt().toShort()
                pcmBuffer[i * 2 + 1] = (mixR * 32767.0).toInt().toShort()

                musicTime += dt
            }

            val written = runCatching {
                track.write(pcmBuffer, 0, pcmBuffer.size, AudioTrack.WRITE_BLOCKING)
            }.getOrDefault(-1)

            if (written < 0) {
                try {
                    Thread.sleep(30)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
    }

    private fun synthesizeVoiceSample(v: SfxVoice, rng: Random): Double {
        val t = v.ageSec
        return when (v.type) {
            SfxType.DEW_CHIME -> {
                val env = (t / 0.012).coerceIn(0.0, 1.0) * exp(-t * 4.2)
                env * 0.38 * (
                    sin(TWO_PI * v.freqHz * t) +
                        0.3 * sin(TWO_PI * v.secondaryFreqHz * t) * exp(-t * 7.0)
                    )
            }
            SfxType.LANTERN_BLOOM -> {
                val env = (t / 0.05).coerceIn(0.0, 1.0) * exp(-t * 1.65)
                val f3 = v.freqHz * 1.25 // Major third
                val f5 = v.secondaryFreqHz // Octave/Fifth shimmer
                env * 0.34 * (
                    sin(TWO_PI * v.freqHz * t) +
                        0.75 * sin(TWO_PI * f3 * t) +
                        0.6 * sin(TWO_PI * f5 * t) +
                        0.25 * sin(TWO_PI * (f5 * 1.5) * t)
                    )
            }
            SfxType.TREE_RUSTLE -> {
                val env = (t / 0.04).coerceIn(0.0, 1.0) * exp(-t * 4.5)
                val softNoise = rng.nextDouble(-0.18, 0.18) * exp(-t * 7.5)
                val chime = 0.22 * sin(TWO_PI * v.freqHz * t) + 0.14 * sin(TWO_PI * v.secondaryFreqHz * t)
                env * (chime + softNoise)
            }
            SfxType.THREAD_SHIMMER -> {
                val env = (t / 0.025).coerceIn(0.0, 1.0) * exp(-t * 7.5)
                env * 0.22 * (sin(TWO_PI * v.freqHz * t) + 0.4 * sin(TWO_PI * v.secondaryFreqHz * t))
            }
            SfxType.UI_TAP -> {
                val env = (t / 0.006).coerceIn(0.0, 1.0) * exp(-t * 24.0)
                env * 0.26 * sin(TWO_PI * v.freqHz * t)
            }
            SfxType.SPIRIT_COO -> {
                val env = (t / 0.035).coerceIn(0.0, 1.0) * exp(-t * 3.8)
                val glide = if (t < 0.22) v.freqHz else v.secondaryFreqHz
                env * 0.28 * sin(TWO_PI * glide * t)
            }
        }
    }
}
