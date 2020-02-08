package ru.radiationx.data.datasource.remote

interface IApiUtils {
    fun toHtml(text: String?): CharSequence?
    fun escapeHtml(text: String?): String?
}
