package ru.radiationx.data.entity.domain.schedule

import ru.radiationx.data.apinext.models.enums.PublishDay
import ru.radiationx.data.entity.domain.feed.ScheduleItem
import java.util.Calendar

data class ScheduleDay(
    val day: PublishDay,
    val items: List<ScheduleItem>,
) {
    companion object {
        private const val MONDAY = "1"
        private const val TUESDAY = "2"
        private const val WEDNESDAY = "3"
        private const val THURSDAY = "4"
        private const val FRIDAY = "5"
        private const val SATURDAY = "6"
        private const val SUNDAY = "7"

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