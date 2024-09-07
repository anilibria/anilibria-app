package anilibria.api.shared


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectionReleaseIdNetwork(
    @Json(name = "release_id")
    val releaseId: Int,
    @Json(name = "type_of_collection")
    val typeOfCollection: String
)