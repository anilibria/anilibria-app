package ru.radiationx.anilibria.screen.search.sort

import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import ru.radiationx.data.api.shared.filter.legacy.SearchForm
import javax.inject.Inject

class SearchSortViewModel @Inject constructor(
    argExtra: SearchSortExtra,
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    private val titles = listOf(
        "По популярности",
        "По новизне"
    )

    val titlesData = MutableStateFlow<List<String>>(emptyList())
    val selectedIndex = MutableStateFlow<Int?>(null)

    init {
        titlesData.value = titles
        selectedIndex.value = when (argExtra.sort) {
            SearchForm.Sort.RATING -> 0
            SearchForm.Sort.DATE -> 1
        }
    }

    fun applySort(index: Int) {
        guidedRouter.close()
        val sort = when (index) {
            0 -> SearchForm.Sort.RATING
            1 -> SearchForm.Sort.DATE
            else -> null
        }
        sort?.also {
            searchController.sortEvent.emit(it)
        }
    }
}