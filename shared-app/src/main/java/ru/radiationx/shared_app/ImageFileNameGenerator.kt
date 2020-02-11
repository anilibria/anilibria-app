package ru.radiationx.shared_app

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import java.util.regex.Pattern

class ImageFileNameGenerator : HashCodeFileNameGenerator() {

    private val pattern by lazy {
        Pattern.compile("(?:[a-zA-Z0-9-_]+\\.)*[a-zA-Z0-9][a-zA-Z0-9-_]+\\.[a-zA-Z]{2,11}?\\/\\??([\\s\\S]*)$")
    }

    override fun generate(imageUri: String?): String = localGenerate(imageUri)

    private fun localGenerate(imageUri: String?): String {
        try {
            val pathMatcher = pattern.matcher(imageUri)
            if (pathMatcher.find()) {
                val path = pathMatcher.group(1)
                if (!path.isNullOrEmpty()) {
                    return path.hashCode().toString()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return super.generate(imageUri)
    }
}