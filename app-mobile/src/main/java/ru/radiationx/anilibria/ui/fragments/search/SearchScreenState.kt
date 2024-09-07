package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderState

data class SearchScreenState(
    val remindText: String? = null,
    val data: PageLoaderState<List<ReleaseItemState>> = PageLoaderState()
)