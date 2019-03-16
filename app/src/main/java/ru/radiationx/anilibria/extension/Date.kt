package ru.radiationx.anilibria.extension

import java.text.SimpleDateFormat
import java.util.*

private object DateFormats {
    val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeSecFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
}

fun Date.asDateTimeString(): String = DateFormats.dateTimeFormat.format(this)
fun Date.asDateString(): String = DateFormats.dateFormat.format(this)
fun Date.asTimeString(): String = DateFormats.timeFormat.format(this)
fun Date.asTimeSecString(): String = DateFormats.timeSecFormat.format(this)

fun Date.isSameDay(date: Date): Boolean {
    val cal1: Calendar = Calendar.getInstance()
    cal1.time = this
    val cal2: Calendar = Calendar.getInstance()
    cal2.time = date
    return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
