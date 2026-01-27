package ru.radiationx.media.mobile.utils

import java.util.concurrent.TimeUnit
import java.util.Locale
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
                append("%d:%02d:%02d".format(Locale.US, hours, minutes, seconds))
            } else {
                append("%02d:%02d".format(Locale.US, minutes, seconds))
            }
        }
    }
}