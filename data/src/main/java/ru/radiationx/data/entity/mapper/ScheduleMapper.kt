package ru.radiationx.data.entity.mapper

import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.feed.ScheduleItem
import ru.radiationx.data.entity.app.schedule.ScheduleDay
import ru.radiationx.data.entity.response.schedule.ScheduleDayResponse
import ru.radiationx.data.system.ApiUtils

fun ScheduleDayResponse.toDomain(
    apiUtils: ApiUtils,
    apiConfig: ApiConfig
): ScheduleDay = ScheduleDay(
    day = ScheduleDay.toCalendarDay(day),
    items = items.map {
        ScheduleItem(it.toDomain(apiUtils, apiConfig))
    }
)