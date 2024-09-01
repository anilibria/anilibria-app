package ru.radiationx.data.apinext.datasources

import anilibria.api.schedule.ScheduleApi
import ru.radiationx.data.apinext.toDomain
import ru.radiationx.data.entity.domain.schedule.ScheduleDay

class ScheduleApiDataSource(
    private val api: ScheduleApi
) {

    suspend fun getWeek(): List<ScheduleDay> {
        return api.getWeek().toDomain()
    }
}