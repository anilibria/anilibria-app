package ru.radiationx.anilibria.screen.search.year

import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.search.BaseSearchValuesViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import ru.radiationx.data.entity.app.release.YearItem
import ru.radiationx.data.repository.SearchRepository
import toothpick.InjectConstructor

@InjectConstructor
class SearchYearViewModel(
    private val searchRepository: SearchRepository,
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter
) : BaseSearchValuesViewModel() {

    private val currentYears = mutableListOf<YearItem>()

    override fun onColdCreate() {
        super.onColdCreate()
        searchRepository
            .observeYears()
            .distinctUntilChanged()
            .lifeSubscribe {
                currentYears.clear()
                currentYears.addAll(it)
                currentValues.clear()
                currentValues.addAll(it.map { it.value })
                valuesData.value = it.map { it.title }
                progressState.value = false
                updateChecked()
                updateSelected()
            }
    }

    override fun onCreate() {
        super.onCreate()
        progressState.value = true
        searchRepository
            .getYears()
            .doFinally { progressState.value = false }
            .lifeSubscribe({}, {})
    }

    override fun applyValues() {
        searchController.yearsEvent.accept(currentYears.filterIndexed { index, item -> checkedValues.contains(item.value) })
        guidedRouter.close()
    }
}