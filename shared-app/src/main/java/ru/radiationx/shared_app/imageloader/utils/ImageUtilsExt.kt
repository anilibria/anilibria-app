package ru.radiationx.shared_app.imageloader.utils

import ru.radiationx.data.common.Url

fun Url?.toCacheKey(): String? = when (this) {
    is Url.Path -> value
    is Url.Absolute -> value
    null -> null
}