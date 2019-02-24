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
