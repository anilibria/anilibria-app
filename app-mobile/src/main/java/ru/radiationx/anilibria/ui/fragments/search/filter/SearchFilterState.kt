package ru.radiationx.anilibria.ui.fragments.search.filter

import ru.radiationx.data.api.shared.filter.FilterData
import ru.radiationx.data.api.shared.filter.FilterForm
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoaderState

data class SearchFilterState(
    val filter: SingleLoaderState<FilterData> = SingleLoaderState.empty(),
    val form: FilterForm = FilterForm.empty()
)