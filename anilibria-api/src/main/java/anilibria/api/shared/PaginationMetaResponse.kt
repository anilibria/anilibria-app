package anilibria.api.shared


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaginationMetaResponse(
    @Json(name = "pagination")
    val pagination: PaginationInfoResponse
) {

}