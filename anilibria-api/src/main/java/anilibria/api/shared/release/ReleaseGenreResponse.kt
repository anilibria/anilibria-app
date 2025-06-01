package anilibria.api.shared.release

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseGenreResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
)