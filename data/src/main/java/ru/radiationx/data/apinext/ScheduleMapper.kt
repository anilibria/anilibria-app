package ru.radiationx.data.apinext

import anilibria.api.schedule.models.ScheduleResponse
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import ru.radiationx.data.entity.domain.schedule.ScheduleDay

fun List<ScheduleResponse>.toDomain(): List<ScheduleDay> {
    return map { it.release.toDomain() }
        .sortedBy { it.publishDay }
        .groupBy { it.publishDay }
        .map { (publishDay, releases) ->
            ScheduleDay(
                ScheduleDay.toCalendarDay(publishDay),
                releases.map { ScheduleItem(it) }
            )
        }
}