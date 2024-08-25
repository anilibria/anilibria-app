package anilibria.api.shared


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaginationInfoResponse(
    @Json(name = "total")
    val total: Int,
    @Json(name = "count")
    val count: Int,
    @Json(name = "per_page")
    val perPage: Int,
    @Json(name = "current_page")
    val currentPage: Int,
    @Json(name = "total_pages")
    val totalPages: Int,
    @Json(name = "links")
    val links: Links
) {
    @JsonClass(generateAdapter = true)
    data class Links(
        @Json(name = "previous")
        val previous: String?,
        @Json(name = "next")
        val next: String?
    )
}