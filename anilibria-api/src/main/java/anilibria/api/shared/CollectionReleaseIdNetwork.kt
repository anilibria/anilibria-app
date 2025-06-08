package anilibria.api.shared


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectionReleaseIdNetwork(
    @Json(name = "release_id")
    val releaseId: Int,
    @Json(name = "type_of_collection")
    val typeOfCollection: String
) {
    companion object {
        fun ofList(list: List<Any>): CollectionReleaseIdNetwork {
            return CollectionReleaseIdNetwork(
                releaseId = (list[0] as Number).toInt(),
                typeOfCollection = (list[1] as String)
            )
        }
    }
}