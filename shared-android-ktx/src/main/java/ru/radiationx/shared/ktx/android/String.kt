package ru.radiationx.shared.ktx.android

import android.graphics.Color
import android.util.Base64
import java.nio.charset.StandardCharsets
import androidx.core.graphics.toColorInt

fun String?.toBase64(): String? {
    return this?.let {
        Base64.encodeToString(it.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
    }
}

fun String.parseColorOrNull(): Int? {
    return try {
        this.toColorInt()
    } catch (ignore: Exception) {
        null
    }
}