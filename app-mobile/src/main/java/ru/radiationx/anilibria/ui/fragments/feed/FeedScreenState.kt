package ru.radiationx.anilibria.ui.fragments.feed

import ru.radiationx.anilibria.model.FeedItemState
import ru.radiationx.anilibria.model.ScheduleItemState

data class FeedScreenState(
    val emptyLoading: Boolean = false,
    val refreshing: Boolean = false,
    val hasMorePages: Boolean = false,
    val hasError: Boolean = false,
    val feedItems: List<FeedItemState> = emptyList(),
    val schedule: FeedScheduleState? = null
)

data class FeedScheduleState(
    val title: String,
    val items: List<ScheduleItemState>
)