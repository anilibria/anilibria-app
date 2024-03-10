package ru.radiationx.anilibria.ui.fragments.feed

import ru.radiationx.anilibria.model.DonationCardItemState
import ru.radiationx.anilibria.model.FeedItemState
import ru.radiationx.anilibria.model.ScheduleItemState
import ru.radiationx.anilibria.ads.NativeAdItem
import ru.radiationx.anilibria.model.loading.DataLoadingState


data class FeedScreenState(
    val data: DataLoadingState<FeedDataState> = DataLoadingState(),
    val warnings: List<FeedAppWarning> = emptyList(),
    val donationCardItemState: DonationCardItemState? = null
)

data class FeedDataState(
    val feedItems: List<NativeAdItem<FeedItemState>> = emptyList(),
    val schedule: FeedScheduleState? = null
)

data class FeedScheduleState(
    val title: String,
    val items: List<ScheduleItemState>
)

data class FeedAppWarning(
    val tag: String,
    val title: String,
    val type: FeedAppWarningType
)

enum class FeedAppWarningType {
    INFO,
    WARNING
}