package ru.radiationx.data.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Url : Parcelable {

    companion object {
        fun pathOf(raw: String): Path {
            return Path(raw)
        }

        fun absoluteOf(raw: String): Absolute {
            return Absolute(raw)
        }
    }

    val value: String

    fun absolute(baseUrl: BaseUrl): String

    @Parcelize
    data class Path(private val path: String) : Url {

        override val value: String
            get() = path.trimStart('/')

        override fun absolute(baseUrl: BaseUrl): String {
            return "${baseUrl.value}${value}"
        }

        override fun toString(): String {
            return value
        }
    }

    @Parcelize
    data class Absolute(private val url: String) : Url {

        override val value: String
            get() = url

        override fun absolute(baseUrl: BaseUrl): String {
            return value
        }

        override fun toString(): String {
            return value
        }
    }
}

fun String.toPathUrl(): Url.Path {
    return Url.pathOf(this)
}

fun String.toAbsoluteUrl(): Url.Absolute {
    return Url.Absolute(this)
}

