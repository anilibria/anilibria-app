package ru.radiationx.data.api.schedule

import anilibria.api.schedule.ScheduleApi
import ru.radiationx.data.api.schedule.mapper.toDomain
import ru.radiationx.data.api.schedule.models.ScheduleDay
import javax.inject.Inject

class ScheduleApiDataSource @Inject constructor(
    private val api: ScheduleApi
) {

    suspend fun getWeek(): List<ScheduleDay> {
        return api.getWeek().toDomain()
    }
}