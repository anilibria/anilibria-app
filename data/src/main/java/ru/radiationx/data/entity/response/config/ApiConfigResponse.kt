package ru.radiationx.data.entity.response.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiConfigResponse(
    @Json(name = "addresses") val addresses: List<ApiConfigAddressResponse>
)
