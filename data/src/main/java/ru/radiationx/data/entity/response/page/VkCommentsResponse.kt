package ru.radiationx.data.entity.response.page

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VkCommentsResponse(
    @Json(name = "baseUrl") val baseUrl: String,
    @Json(name = "script") val script: String
)