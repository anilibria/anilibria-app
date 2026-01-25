package ru.radiationx.media.mobile.utils

import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

internal object TimeFormatter {
    private val oneHour = TimeUnit.HOURS.toMillis(1)
    private val oneMinute = TimeUnit.MINUTES.toMillis(1)

    fun format(time: Long, withSign: Boolean = false): String {
        var temp = time.absoluteValue
        val hours = TimeUnit.MILLISECONDS.toHours(temp)
        temp -= hours * oneHour
        val minutes = TimeUnit.MILLISECONDS.toMinutes(temp)
        temp -= minutes * oneMinute
        val seconds = TimeUnit.MILLISECONDS.toSeconds(temp)
        return buildString {
            if (withSign) {
                if (time < 0) {
                    append('-')
                } else {
                    append('+')
                }
            }
            if (hours > 0) {
                append(hours)
                append(':')
            }
            if (minutes < 10) {
                append(0)
            }
            append(minutes)
            append(':')
            if (seconds < 10) {
                append(0)
            }
            append(seconds)
        }
    }
}