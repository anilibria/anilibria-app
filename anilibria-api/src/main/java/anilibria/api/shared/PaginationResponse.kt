package anilibria.api.shared


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaginationResponse<T>(
    @Json(name = "data")
    val data: List<T>,
    @Json(name = "meta")
    val meta: PaginationMetaResponse
)