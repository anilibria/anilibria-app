package ru.radiationx.anilibria.screen.search.genre

import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.search.BaseSearchValuesViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import ru.radiationx.data.entity.app.release.GenreItem
import ru.radiationx.data.repository.SearchRepository
import toothpick.InjectConstructor

@InjectConstructor
class SearchGenreViewModel(
    private val searchRepository: SearchRepository,
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter
) : BaseSearchValuesViewModel() {

    private val currentGenres = mutableListOf<GenreItem>()

    override fun onColdCreate() {
        super.onColdCreate()
        searchRepository
            .observeGenres()
            .distinctUntilChanged()
            .lifeSubscribe {
                currentGenres.clear()
                currentGenres.addAll(it)
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
            .getGenres()
            .doFinally { progressState.value = false }
            .lifeSubscribe({}, {})
    }

    override fun applyValues() {
        searchController.genresEvent.accept(currentGenres.filterIndexed { index, item -> checkedValues.contains(item.value) })
        guidedRouter.close()
    }
}