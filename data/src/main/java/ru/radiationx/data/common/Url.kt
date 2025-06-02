package ru.radiationx.data.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface Url : Parcelable {

    companion object {

        private fun concat(baseUrl: Base, path: Path): String {
            return "${baseUrl.value}${path.value}"
        }

        fun baseOf(baseUrl: String): Base {
            return Base(baseUrl)
        }

        fun pathOf(path: String): Path {
            return Path(path)
        }

        fun absoluteOf(url: String): Absolute {
            return Absolute(url)
        }
    }

    val value: String

    @Parcelize
    data class Base(private val baseUrl: String) : Url {

        override val value: String
            get() = baseUrl.trimEnd('/') + "/"

        fun withPath(path: Path): String {
            return concat(this, path)
        }

        override fun toString(): String {
            return value
        }
    }

    @Parcelize
    data class Path(private val path: String) : Url {

        override val value: String
            get() = path.trimStart('/')

        fun withBase(baseUrl: Base): String {
            return concat(baseUrl, this)
        }

        override fun toString(): String {
            return value
        }
    }

    @Parcelize
    data class Absolute(private val url: String) : Url {

        override val value: String
            get() = url

        override fun toString(): String {
            return value
        }
    }
}


fun Url.withBase(baseUrl: Url.Base): String {
    return when (this) {
        is Url.Base -> this.value
        is Url.Path -> this.withBase(baseUrl)
        is Url.Absolute -> this.value
    }
}

fun String.toPathUrl(): Url.Path {
    return Url.pathOf(this)
}

fun String.toAbsoluteUrl(): Url.Absolute {
    return Url.absoluteOf(this)
}

fun String.toBaseUrl(): Url.Base {
    return Url.baseOf(this)
}


