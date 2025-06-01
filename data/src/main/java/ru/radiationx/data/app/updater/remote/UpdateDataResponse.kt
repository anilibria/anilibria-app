package ru.radiationx.data.app.updater.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateDataRootResponse(
    @Json(name = "update") val update: UpdateDataResponse
)

@JsonClass(generateAdapter = true)
data class UpdateDataResponse(
    @Json(name = "version_code") val code: String,
    @Json(name = "version_build") val build: String,
    @Json(name = "version_name") val name: String,
    @Json(name = "build_date") val date: String,
    @Json(name = "links") val links: List<UpdateLink>,
    @Json(name = "important") val important: List<String>,
    @Json(name = "added") val added: List<String>,
    @Json(name = "fixed") val fixed: List<String>,
    @Json(name = "changed") val changed: List<String>
) {

    @JsonClass(generateAdapter = true)
    data class UpdateLink(
        @Json(name = "name") val name: String,
        @Json(name = "url") val url: String,
        @Json(name = "type") val type: String
    )
}