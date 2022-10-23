package ru.radiationx.anilibria.screen.search.completed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import toothpick.InjectConstructor

@InjectConstructor
class SearchCompletedViewModel(
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    private val titles = listOf(
        "Все",
        "Только завершенные"
    )

    var argCompleted: Boolean = false

    val titlesData = MutableLiveData<List<String>>()
    val selectedIndex = MutableLiveData<Int>()

    override fun onCreate() {
        super.onCreate()
        titlesData.value = titles
        selectedIndex.value = if (argCompleted) 1 else 0
    }

    fun applySort(index: Int) {
        viewModelScope.launch {
            val sort = when (index) {
                0 -> false
                1 -> true
                else -> null
            }
            sort?.also {
                searchController.completedEvent.emit(it)
            }
            guidedRouter.close()
        }
    }
}