package ru.radiationx.anilibria.screen.search.completed

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import toothpick.InjectConstructor

@InjectConstructor
class SearchCompletedViewModel(
    private val argExtra: SearchCompletedExtra,
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter
) : LifecycleViewModel() {

    private val titles = listOf(
        "Все",
        "Только завершенные"
    )

    val titlesData = MutableLiveData<List<String>>()
    val selectedIndex = MutableLiveData<Int>()

    init {
        titlesData.value = titles
        selectedIndex.value = if (argExtra.isCompleted) 1 else 0
    }

    fun applySort(index: Int) {
        guidedRouter.close()
        val sort = when (index) {
            0 -> false
            1 -> true
            else -> null
        }
        sort?.also {
            searchController.completedEvent.emit(it)
        }
    }
}