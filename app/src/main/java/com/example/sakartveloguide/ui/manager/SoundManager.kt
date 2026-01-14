package com.example.sakartveloguide.ui.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(private val context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val sounds = mutableMapOf<String, Int>()

    fun playStampSlam() {
        sounds["stamp_slam"]?.let { id ->
            soundPool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playTick() {
        sounds["click_tick"]?.let { id ->
            soundPool.play(id, 0.5f, 0.5f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}