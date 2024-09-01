package anilibria.api.schedule

import anilibria.api.schedule.models.ScheduleNowResponse
import anilibria.api.schedule.models.ScheduleResponse
import retrofit2.http.GET

interface ScheduleApi {

    @GET("/anime/schedule/now")
    suspend fun getNow(): ScheduleNowResponse

    @GET("/anime/schedule/week")
    suspend fun getWeek(): List<ScheduleResponse>
}