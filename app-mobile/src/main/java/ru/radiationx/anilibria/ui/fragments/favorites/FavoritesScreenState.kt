package ru.radiationx.anilibria.ui.fragments.favorites

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderState

data class FavoritesScreenState(
    val searchItems: List<ReleaseItemState> = emptyList(),
    val deletingItemIds: List<ReleaseId> = emptyList(),
    val data: PageLoaderState<List<ReleaseItemState>> = PageLoaderState.empty()
)