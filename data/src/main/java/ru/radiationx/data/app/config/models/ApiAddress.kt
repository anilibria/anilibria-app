package ru.radiationx.data.app.config.models

data class ApiAddress(
    val tag: String,
    val name: String?,
    val desc: String?,
    val widgetsSite: String,
    val site: String,
    val baseImages: String,
    val base: String,
    val api: String,
    val ips: List<String>,
    val proxies: List<ApiProxy>
)