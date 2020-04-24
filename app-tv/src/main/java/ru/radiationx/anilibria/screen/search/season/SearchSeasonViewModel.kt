package ru.radiationx.anilibria.screen.search.season

import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.search.BaseSearchValuesViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import ru.radiationx.data.entity.app.release.SeasonItem
import ru.radiationx.data.repository.SearchRepository
import toothpick.InjectConstructor

@InjectConstructor
class SearchSeasonViewModel(
    private val searchRepository: SearchRepository,
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter
) : BaseSearchValuesViewModel() {

    private val currentSeasons = mutableListOf<SeasonItem>()

    override fun onCreate() {
        super.onCreate()
        searchRepository
            .getSeasons()
            .lifeSubscribe({
                currentSeasons.clear()
                currentSeasons.addAll(it)
                currentValues.clear()
                currentValues.addAll(it.map { it.value })
                valuesData.value = it.map { it.title }
                updateChecked()
                updateSelected()
            }, {})
    }

    override fun applyValues() {
        searchController.seasonsEvent.accept(currentSeasons.filterIndexed { index, item -> checkedValues.contains(item.value) })
        guidedRouter.close()
    }
}