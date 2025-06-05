package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.data.api.shared.filter.FilterData
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderState
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoaderState

data class SearchScreenState(
    val releases: PageLoaderState<List<ReleaseItemState>> = PageLoaderState.empty(),
    val filter: SingleLoaderState<FilterData> = SingleLoaderState.empty()
)