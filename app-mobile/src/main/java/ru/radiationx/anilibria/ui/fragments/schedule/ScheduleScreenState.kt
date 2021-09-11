package ru.radiationx.anilibria.ui.fragments.schedule

import ru.radiationx.anilibria.model.ScheduleItemState

data class ScheduleScreenState(
    val refreshing: Boolean = false,
    val dayItems: List<ScheduleDayState> = emptyList()
)

data class ScheduleDayState(
    val title: String,
    val items: List<ScheduleItemState> = emptyList()
)