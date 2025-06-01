package ru.radiationx.data.app.downloader.db

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteFileDb(
    @Json(name = "id") val id: String,
    @Json(name = "url") val url: String,
    @Json(name = "bucketName") val bucketName: String,
    @Json(name = "bucketReleaseId") val bucketReleaseId: Int?,
    @Json(name = "contentDisposition") val contentDisposition: String?,
    @Json(name = "contentType") val contentType: String?,
)
