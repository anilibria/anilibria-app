package ru.radiationx.data.entity.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaginatedResponse<out T>(
    @Json(name = "items") val data: T,
    @Json(name = "pagination") val meta: PaginationResponse,
) {

    @JsonClass(generateAdapter = true)
    data class PaginationResponse(
        @Json(name = "page") val page: Int?,
        @Json(name = "allPages") val allPages: Int?,
        @Json(name = "perPage") val perPage: Int?,
        @Json(name = "allItems") val allItems: Int?
    )
}
