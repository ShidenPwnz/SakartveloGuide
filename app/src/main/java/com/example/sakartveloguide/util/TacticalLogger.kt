package com.example.sakartveloguide.util

import android.util.Log
import com.example.sakartveloguide.BuildConfig

object TacticalLogger {
    private const val TAG = "SAKARTVELO_LOG"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "[DEBUG] $message")
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        // Errors should be logged to Firebase Crashlytics in production
        Log.e(TAG, "[ERROR] $message", throwable)
    }

    fun i(message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "[INFO] $message")
        }
    }
}