package ru.radiationx.data.datasource.remote

data class NetworkResponse(
    val url: String,
    val code: Int,
    val message: String,
    val redirect: String,
    val body: String,
    val hostIp: String?
)
