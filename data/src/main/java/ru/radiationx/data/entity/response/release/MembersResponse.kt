package ru.radiationx.data.entity.response.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MembersResponse(
    @Json(name = "timing") val timing: List<String>,
    @Json(name = "voicing") val voicing: List<String>,
    @Json(name = "editing") val editing: List<String>,
    @Json(name = "decorating") val decorating: List<String>,
    @Json(name = "translating") val translating: List<String>,
)