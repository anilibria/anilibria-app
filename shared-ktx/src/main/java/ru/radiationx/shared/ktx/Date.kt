package ru.radiationx.shared.ktx

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private object DateFormats {
    val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeSecFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    val timeHourMinuteSecFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeSecFormatUtc = SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val timeHourMinuteSecFormatUtc = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}

fun Date.asDateTimeString(): String = DateFormats.dateTimeFormat.format(this)
fun Date.asDateString(): String = DateFormats.dateFormat.format(this)
fun Date.asTimeString(): String = DateFormats.timeFormat.format(this)
fun Date.asTimeSecString(): String {
    return if (time >= TimeUnit.HOURS.toMillis(1)) {
        DateFormats.timeHourMinuteSecFormatUtc.format(this)
    } else {
        DateFormats.timeSecFormatUtc.format(this)
    }
}

fun Long.asUtc(): Long = this - TimeZone.getDefault().getOffset(this)
fun Long.asMsk() = this.asUtc() + TimeUnit.HOURS.toMillis(3)

fun Long.getDayOfWeek() = Calendar.getInstance().also {
    it.timeInMillis = this
}.get(Calendar.DAY_OF_WEEK)

fun Date.isSameDay(date: Date): Boolean {
    val cal1: Calendar = Calendar.getInstance()
    cal1.time = this
    val cal2: Calendar = Calendar.getInstance()
    cal2.time = date
    return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
