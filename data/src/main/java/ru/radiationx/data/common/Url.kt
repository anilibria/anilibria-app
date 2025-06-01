package ru.radiationx.data.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Url : Parcelable {

    companion object {
        fun relativeOf(raw: String): Relative {
            return Relative(raw)
        }

        fun absoluteOf(raw: String): Absolute {
            return Absolute(raw)
        }
    }

    val raw: String

    fun absolute(baseUrl: String): String

    @Parcelize
    data class Relative(override val raw: String) : Url {

        override fun absolute(baseUrl: String): String {
            val trimmedBase = baseUrl.trimEnd('/')
            val trimmedRaw = raw.trimStart('/')
            return "${trimmedBase}/${trimmedRaw}"
        }
    }

    @Parcelize
    data class Absolute(override val raw: String) : Url {

        override fun absolute(baseUrl: String): String {
            return raw
        }
    }
}

fun String.toRelativeUrl(): Url.Relative {
    return Url.relativeOf(this)
}

fun String.toAbsoluteUrl(): Url.Absolute {
    return Url.Absolute(this)
}

