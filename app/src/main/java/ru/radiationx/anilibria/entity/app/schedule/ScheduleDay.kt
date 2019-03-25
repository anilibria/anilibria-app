package ru.radiationx.anilibria.entity.app.schedule

import ru.radiationx.anilibria.entity.app.feed.ScheduleItem
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import java.lang.Exception
import java.util.*

data class ScheduleDay(
        val day: Int,
        val items: List<ScheduleItem>
) {
    companion object {
        const val MONDAY = "1"
        const val TUESDAY = "2"
        const val WEDNESDAY = "3"
        const val THURSDAY = "4"
        const val FRIDAY = "5"
        const val SATURDAY = "6"
        const val SUNDAY = "7"

        fun fromCalendarDay(day: Int): String = when (day) {
            Calendar.MONDAY -> MONDAY
            Calendar.TUESDAY -> TUESDAY
            Calendar.WEDNESDAY -> WEDNESDAY
            Calendar.THURSDAY -> THURSDAY
            Calendar.FRIDAY -> FRIDAY
            Calendar.SATURDAY -> SATURDAY
            Calendar.SUNDAY -> SUNDAY
            else -> throw Exception("Not found schedule day by $day")
        }

        fun toCalendarDay(day: String): Int = when (day) {
            MONDAY -> Calendar.MONDAY
            TUESDAY -> Calendar.TUESDAY
            WEDNESDAY -> Calendar.WEDNESDAY
            THURSDAY -> Calendar.THURSDAY
            FRIDAY -> Calendar.FRIDAY
            SATURDAY -> Calendar.SATURDAY
            SUNDAY -> Calendar.SUNDAY
            else -> throw Exception("Not found calendar day by $day")
        }
    }
}