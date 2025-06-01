package ru.radiationx.data.api.schedule.models

data class ScheduleDay(
    val day: PublishDay,
    val items: List<ScheduleItem>,
)