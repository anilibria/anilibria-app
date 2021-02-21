package ru.radiationx.data.analytics

import android.os.SystemClock

class TimeCounter {
    private var counted = 0L
    private var lastTime: Long? = null

    fun start() {
        counted = 0
        lastTime = SystemClock.elapsedRealtime()
    }

    fun pause() {
        val delta = getDelta() ?: return
        lastTime = null
        counted += delta
    }

    fun resume() {
        lastTime = SystemClock.elapsedRealtime()
    }

    fun elapsed(): Long {
        val delta = getDelta() ?: 0L
        return counted + delta
    }

    private fun getDelta(): Long? {
        return lastTime?.let { SystemClock.elapsedRealtime() - it }
    }
}