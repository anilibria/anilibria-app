package ru.radiationx.anilibria.ui.fragments.history

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingState

data class HistoryScreenState(
    val searchItems: List<ReleaseItemState> = emptyList(),
    val data: DataLoadingState<List<ReleaseItemState>> = DataLoadingState()
)