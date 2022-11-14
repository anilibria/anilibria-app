package ru.radiationx.anilibria.utils

import java.text.DecimalFormat
import kotlin.math.pow

object Utils {

    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()

        val number = size / 1024.0.pow(digitGroups.toDouble())
        val formattedNumber = DecimalFormat("#,##0.#").format(number)
        val unit = units[digitGroups]
        return "$formattedNumber $unit"
    }
}
