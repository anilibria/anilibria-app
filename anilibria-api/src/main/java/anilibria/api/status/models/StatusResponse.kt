package anilibria.api.status.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusResponse(
    @Json(name = "request")
    val request: Request?,
    @Json(name = "is_alive")
    val isAlive: Boolean?,
    @Json(name = "available_api_endpoints")
    val availableApiEndpoints: List<String>?
) {

    @JsonClass(generateAdapter = true)
    data class Request(
        @Json(name = "ip")
        val ip: String?,
        @Json(name = "country")
        val country: String?,
        @Json(name = "iso_code")
        val isoCode: String?,
        @Json(name = "timezone")
        val timeZone: String?
    )
}