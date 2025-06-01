package anilibria.api.shared.errors

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidationApiError(
    @Json(name = "errors")
    val errors: Map<String, List<String>>
)