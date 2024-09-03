package ru.radiationx.data.datasource.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class ApiError(
    val code: Int?,
    override val message: String?,
    val description: String?
) : RuntimeException()


@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @Json(name = "code") val code: Int?,
    @Json(name = "message") val message: String?,
    @Json(name = "description") val description: String?
)
