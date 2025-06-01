package anilibria.api.shared.errors

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommonApiError(
    @Json(name = "message")
    val rawMessage: String?,
    @Json(name = "error")
    val rawError: String?
) {
    val message: String = requireNotNull(rawMessage ?: rawError)
}