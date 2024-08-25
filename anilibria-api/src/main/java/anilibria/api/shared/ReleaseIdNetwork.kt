package anilibria.api.shared
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseIdNetwork(
    @Json(name = "release_id")
    val releaseId: Int
)