package ru.radiationx.data.entity.response.schedule

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.radiationx.data.entity.response.release.ReleaseResponse

@JsonClass(generateAdapter = true)
data class ScheduleDayResponse(
    @Json(name = "day") val day: String,
    @Json(name = "items") val items: List<ReleaseResponse>
)