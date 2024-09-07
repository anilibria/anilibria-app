package ru.radiationx.anilibria.ui.fragments.history

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderState

data class HistoryScreenState(
    val searchItems: List<ReleaseItemState> = emptyList(),
    val data: PageLoaderState<List<ReleaseItemState>> = PageLoaderState()
)