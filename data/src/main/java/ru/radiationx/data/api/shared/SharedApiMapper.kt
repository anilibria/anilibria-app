package ru.radiationx.data.api.shared

import java.text.SimpleDateFormat
import java.util.Date

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")

fun String.apiDateToDate(): Date {
    return dateFormat.parse(this)
}

fun Date.dateToSec(): Int = (time / 1000).toInt()

fun Int.secToDate(): Date = Date(secToMillis())

fun Int.secToMillis(): Long = this * 1000L

