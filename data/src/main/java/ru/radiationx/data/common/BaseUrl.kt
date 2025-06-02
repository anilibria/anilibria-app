package ru.radiationx.data.common

data class BaseUrl(
    private val url: String
) {
    val value: String
        get() = url.trimEnd('/') + "/"

    override fun toString(): String {
        return value
    }
}

fun String.toBaseUrl(): BaseUrl {
    return BaseUrl(this)
}