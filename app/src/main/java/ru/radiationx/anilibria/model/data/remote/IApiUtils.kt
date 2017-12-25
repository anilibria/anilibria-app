package ru.radiationx.anilibria.model.data.remote

interface IApiUtils {
    fun toHtml(text: String?): CharSequence?
    fun escapeHtml(text: String?): String?
}
