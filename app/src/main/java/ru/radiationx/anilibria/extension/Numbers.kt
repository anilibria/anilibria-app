package ru.radiationx.anilibria.extension

fun Int.idOrNull(limit: Int = -1): Int? = if (this > limit) {
    this
} else {
    null
}