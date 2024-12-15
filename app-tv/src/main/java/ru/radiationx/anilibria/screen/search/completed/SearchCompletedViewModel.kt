package ru.radiationx.anilibria.screen.search.completed

import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import javax.inject.Inject

class SearchCompletedViewModel @Inject constructor(
    argExtra: SearchCompletedExtra,
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter,
) : LifecycleViewModel() {

    private val titles = listOf(
        "Все",
        "Только завершенные"
    )

    val titlesData = MutableStateFlow<List<String>>(emptyList())
    val selectedIndex = MutableStateFlow<Int?>(null)

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