package com.example.sakartveloguide.ui.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager(private val context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    private val sounds = mutableMapOf<String, Int>()

    init {
        // Prepare your .wav files in res/raw
        // sounds["stamp_slam"] = soundPool.load(context, com.example.sakartveloguide.R.raw.stamp_slam, 1)
        // sounds["click_tick"] = soundPool.load(context, com.example.sakartveloguide.R.raw.click_tick, 1)
    }

    fun playStampSlam() { sounds["stamp_slam"]?.let { soundPool.play(it, 1f, 1f, 1, 0, 1f) } }

    fun playTick() { sounds["click_tick"]?.let { soundPool.play(it, 0.5f, 0.5f, 1, 0, 1f) } }

    fun release() { soundPool.release() }

}