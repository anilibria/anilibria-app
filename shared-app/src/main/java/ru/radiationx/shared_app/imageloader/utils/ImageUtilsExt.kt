package ru.radiationx.shared_app.imageloader.utils

fun String?.toCacheKey(): String? {
    return ImageCacheKeyGenerator.generate(this)
}