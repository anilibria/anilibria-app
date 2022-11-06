package ru.radiationx.anilibria.screen.search.sort

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import ru.radiationx.data.entity.domain.search.SearchForm
import toothpick.InjectConstructor

@InjectConstructor
class SearchSortViewModel(
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    private val titles = listOf(
        "По популярности",
        "По новизне"
    )

    var argSort: SearchForm.Sort? = null

    val titlesData = MutableLiveData<List<String>>()
    val selectedIndex = MutableLiveData<Int>()

    override fun onCreate() {
        super.onCreate()
        titlesData.value = titles
        selectedIndex.value = when (argSort) {
            SearchForm.Sort.RATING -> 0
            SearchForm.Sort.DATE -> 1
            else -> -1
        }
    }

    fun applySort(index: Int) {
        viewModelScope.launch {
            val sort = when (index) {
                0 -> SearchForm.Sort.RATING
                1 -> SearchForm.Sort.DATE
                else -> null
            }
            sort?.also {
                searchController.sortEvent.emit(it)
            }
            guidedRouter.close()
        }
    }
}