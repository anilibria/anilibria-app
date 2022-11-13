package ru.radiationx.shared.ktx.android

import android.app.PendingIntent
import android.os.Build

fun Int.asMutableFlag(): Int = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> this or PendingIntent.FLAG_MUTABLE
    else -> this
}

fun Int.asImmutableFlag(): Int = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> this or PendingIntent.FLAG_IMMUTABLE
    else -> this
}

fun mutableFlag(): Int = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PendingIntent.FLAG_MUTABLE
    else -> 0
}

fun immutableFlag(): Int = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> PendingIntent.FLAG_IMMUTABLE
    else -> 0
}