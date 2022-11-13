package ru.radiationx.shared_app.imageloader.utils

import timber.log.Timber
import java.util.regex.Pattern

object ImageCacheKeyGenerator {

    private val pattern by lazy {
        Pattern.compile("(?:[a-zA-Z0-9-_]+\\.)*[a-zA-Z0-9][a-zA-Z0-9-_]+\\.[a-zA-Z]{2,11}?\\/\\??([\\s\\S]*)$")
    }

    fun generate(imageUri: String?): String? {
        if (imageUri == null) {
            return null
        }
        try {
            val pathMatcher = pattern.matcher(imageUri)
            if (pathMatcher.find()) {
                val path = pathMatcher.group(1)
                if (!path.isNullOrEmpty()) {
                    return path.hashCode().toString()
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
        return imageUri
    }
}