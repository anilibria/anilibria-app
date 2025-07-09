package ru.radiationx.data.api.status.model

data class Status(
    val request: Request?,
    val isAlive: Boolean?,
    val availableApiEndpoints: List<String>?
) {

    data class Request(
        val ip: String?,
        val country: String?,
        val isoCode: String?,
        val timeZone: String?
    )
}