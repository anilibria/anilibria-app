package ru.radiationx.data.apinext

import java.text.SimpleDateFormat
import java.util.Date

private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
fun String.apiDateToDate(): Date {
    return dateFormat.parse(this)
}

