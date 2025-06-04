package ru.radiationx.data.api.shared

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT)

fun String.apiDateToDate(): Date {
    return try {
        requireNotNull(dateFormat.parse(this.toRFC822TimeZone()))
    } catch (ex: Exception) {
        throw IllegalArgumentException("Can't parse date $this", ex)
    }
}


fun Date.toApiDate(): String {
    return try {
        dateFormat.format(this).toISO8601TimeZone()
    } catch (ex: Exception) {
        throw IllegalArgumentException("Can't format date $this", ex)
    }
}

fun String.toRFC822TimeZone(): String {
    if (get(length - 3) != ':') {
        return this
    }
    return "${substring(0, length - 3)}${substring(length - 2, length)}"
}

fun String.toISO8601TimeZone(): String {
    if (get(length - 3) == ':') {
        return this
    }
    return "${substring(0, length - 2)}:${substring(length - 2, length)}"
}


fun Date.dateToSec(): Int = (time / 1000).toInt()

fun Int.secToDate(): Date = Date(secToMillis())

fun Int.secToMillis(): Long = this * 1000L

