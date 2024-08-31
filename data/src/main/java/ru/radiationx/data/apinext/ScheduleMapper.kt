package ru.radiationx.data.apinext

import anilibria.api.schedule.models.ScheduleResponse
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import ru.radiationx.data.entity.domain.schedule.ScheduleDay

fun List<ScheduleResponse>.toDomain(): List<ScheduleDay> {
    val releases = map { it.release.toDomain() }
    val grouped = releases.groupBy { it.publishDay }
    return grouped.map { (publishDay, releases) ->
        ScheduleDay(
            ScheduleDay.toCalendarDay(publishDay),
            releases.map { ScheduleItem(it) }
        )
    }
}