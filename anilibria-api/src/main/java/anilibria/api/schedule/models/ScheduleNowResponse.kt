package anilibria.api.schedule.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduleNowResponse(
    @Json(name = "today")
    val today: List<ScheduleResponse>,
    @Json(name = "tomorrow")
    val tomorrow: List<ScheduleResponse>,
    @Json(name = "yesterday")
    val yesterday: List<ScheduleResponse>
)