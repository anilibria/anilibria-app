package ru.radiationx.data.apinext.models.enums

import java.util.Calendar

enum class PublishDay(val raw: Int, val calendarDay: Int) {
    Monday(1, Calendar.MONDAY),
    Tuesday(2, Calendar.TUESDAY),
    Wednesday(3, Calendar.WEDNESDAY),
    Thursday(4, Calendar.THURSDAY),
    Friday(5, Calendar.FRIDAY),
    Saturday(6, Calendar.SATURDAY),
    Sunday(7, Calendar.SUNDAY);


    companion object {
        fun ofRaw(raw: Int): PublishDay {
            return entries.first { it.raw == raw }
        }

        fun ofCalendar(calendarDay: Int): PublishDay {
            return entries.first { it.calendarDay == calendarDay }
        }
    }
}


fun PublishDay.asDayName() = when (this) {
    PublishDay.Monday -> "Понедельник"
    PublishDay.Tuesday -> "Вторник"
    PublishDay.Wednesday -> "Среда"
    PublishDay.Thursday -> "Четверг"
    PublishDay.Friday -> "Пятница"
    PublishDay.Saturday -> "Суббота"
    PublishDay.Sunday -> "Воскресенье"
}

fun PublishDay.asDayNameDeclension() = when (this) {
    PublishDay.Monday -> "Понедельник"
    PublishDay.Tuesday -> "Вторник"
    PublishDay.Wednesday -> "Среду"
    PublishDay.Thursday -> "Четверг"
    PublishDay.Friday -> "Пятницу"
    PublishDay.Saturday -> "Субботу"
    PublishDay.Sunday -> "Воскресенье"
}

fun PublishDay.asDayPretext() = when (this) {
    PublishDay.Monday -> "в"
    PublishDay.Tuesday -> "во"
    PublishDay.Wednesday -> "в"
    PublishDay.Thursday -> "в"
    PublishDay.Friday -> "в"
    PublishDay.Saturday -> "в"
    PublishDay.Sunday -> "в"
}