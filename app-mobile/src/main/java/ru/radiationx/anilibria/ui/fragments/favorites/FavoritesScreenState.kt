package ru.radiationx.anilibria.ui.fragments.favorites

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingState
import ru.radiationx.data.entity.domain.types.ReleaseId

data class FavoritesScreenState(
    val searchItems: List<ReleaseItemState> = emptyList(),
    val deletingItemIds: List<ReleaseId> = emptyList(),
    val data: DataLoadingState<List<ReleaseItemState>> = DataLoadingState()
)