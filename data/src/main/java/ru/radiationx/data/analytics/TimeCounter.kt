package ru.radiationx.data.analytics

import android.os.SystemClock

class TimeCounter {
    private var counted = 0L
    private var lastTime: Long? = null

    fun start() {
        lastTime = SystemClock.elapsedRealtime()
    }

    fun pause() {
        val delta = getDelta() ?: return
        lastTime = null
        counted += delta
    }

    fun elapsed(): Long {
        val delta = getDelta() ?: 0L
        return counted + delta
    }

    private fun getDelta(): Long? {
        return lastTime?.let { SystemClock.elapsedRealtime() - it }
    }
}