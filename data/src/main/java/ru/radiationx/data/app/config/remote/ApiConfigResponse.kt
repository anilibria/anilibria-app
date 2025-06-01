package ru.radiationx.data.app.config.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiConfigResponse(
    @Json(name = "addresses") val addresses: List<ApiConfigAddressResponse>
)
