package ru.radiationx.shared.ktx

import java.util.Locale

fun String.capitalizeDefault(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }
}