package ru.radiationx.data.entity.mapper

import java.util.Date

fun Date.dateToSec(): Int = (time / 1000).toInt()

fun Int.secToDate(): Date = Date(secToMillis())

fun Int.secToMillis(): Long = this * 1000L