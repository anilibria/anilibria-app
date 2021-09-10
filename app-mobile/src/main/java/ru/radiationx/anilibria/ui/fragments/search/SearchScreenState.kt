package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingState

data class SearchScreenState(
    val remindText: String? = null,
    val data: DataLoadingState<List<ReleaseItemState>> = DataLoadingState()
)