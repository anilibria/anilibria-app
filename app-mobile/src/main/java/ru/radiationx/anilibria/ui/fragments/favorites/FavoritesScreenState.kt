package ru.radiationx.anilibria.ui.fragments.favorites

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingState

data class FavoritesScreenState(
    val searchItems: List<ReleaseItemState> = emptyList(),
    val deletingItemIds: List<Int> = emptyList(),
    val data: DataLoadingState<List<ReleaseItemState>> = DataLoadingState()
)