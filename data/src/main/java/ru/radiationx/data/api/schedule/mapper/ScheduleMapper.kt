package ru.radiationx.data.api.schedule.mapper

import anilibria.api.schedule.models.ScheduleResponse
import ru.radiationx.data.api.releases.mapper.toDomain
import ru.radiationx.data.api.schedule.models.ScheduleDay
import ru.radiationx.data.api.schedule.models.ScheduleItem

fun List<ScheduleResponse>.toDomain(): List<ScheduleDay> {
    return map { it.release.toDomain() }
        .sortedBy { it.publishDay.raw }
        .groupBy { it.publishDay }
        .map { (publishDay, releases) ->
            ScheduleDay(
                day = publishDay,
                items = releases.map { ScheduleItem(it) }
            )
        }
}