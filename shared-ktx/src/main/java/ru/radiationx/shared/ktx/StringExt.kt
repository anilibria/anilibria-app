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

fun String.decapitalizeDefault(): String {
    return replaceFirstChar { it.lowercase(Locale.getDefault()) }
}

fun String.lowercaseDefault(): String {
    return lowercase(Locale.getDefault())
}