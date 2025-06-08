package ru.radiationx.anilibria.ui.fragments.search.tab

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderState

data class SearchTabState(
    val releases: PageLoaderState<List<ReleaseItemState>> = PageLoaderState.empty(),
)