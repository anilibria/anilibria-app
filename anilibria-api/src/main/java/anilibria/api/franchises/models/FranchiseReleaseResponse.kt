package anilibria.api.franchises.models


import anilibria.api.shared.ReleaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FranchiseReleaseResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "sort_order")
    val sortOrder: Int,
    @Json(name = "release_id")
    val releaseId: Int,
    @Json(name = "franchise_id")
    val franchiseId: String,
    @Json(name = "release")
    val release: ReleaseResponse
)