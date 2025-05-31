package ru.radiationx.shared_app.imageloader.utils

import ru.radiationx.data.entity.common.Url

fun Url?.toCacheKey(): String? = when (this) {
    is Url.Relative -> raw
    is Url.Absolute -> raw
    null -> null
}