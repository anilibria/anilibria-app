package ru.radiationx.data.apinext

import anilibria.api.schedule.models.ScheduleResponse
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import ru.radiationx.data.entity.domain.schedule.ScheduleDay

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