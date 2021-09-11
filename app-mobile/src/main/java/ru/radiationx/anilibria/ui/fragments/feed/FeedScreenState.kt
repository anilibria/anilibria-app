package ru.radiationx.anilibria.ui.fragments.feed

import ru.radiationx.anilibria.model.FeedItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.model.loading.DataLoadingState


data class FeedScreenState(
    val data: DataLoadingState<FeedDataState> = DataLoadingState()
)

data class FeedDataState(
    val feedItems: List<FeedItemState> = emptyList(),
    val schedule: FeedScheduleState? = null
)

data class FeedScheduleState(
    val title: String,
    val items: List<ScheduleItemState>
)