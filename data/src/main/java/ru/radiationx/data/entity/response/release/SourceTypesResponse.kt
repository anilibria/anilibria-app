package ru.radiationx.data.entity.response.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SourceTypesResponse(
    @Json(name = "is_rutube") val isRutube: Boolean?,
    @Json(name = "is_anilibria") val isAnilibria: Boolean?
)
