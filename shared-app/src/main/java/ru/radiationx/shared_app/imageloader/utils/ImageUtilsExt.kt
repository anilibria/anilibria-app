package ru.radiationx.shared_app.imageloader.utils

import ru.radiationx.data.common.Url

fun Url?.toCacheKey(): String? {
    return this?.value
}