package ru.radiationx.anilibria.ui.fragments.search.controller

import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.shared.filter.FilterForm
import javax.inject.Inject

class SearchController @Inject constructor() {
    val loaderArg = MutableStateFlow(SearchLoaderArg())
}

data class SearchLoaderArg(
    val query: String = "",
    val form: FilterForm = FilterForm.empty(),
    val collectionType: CollectionType? = null
)