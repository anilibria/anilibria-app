package ru.radiationx.anilibria.screen.search.season

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.search.BaseSearchValuesViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import ru.radiationx.anilibria.screen.search.SearchValuesExtra
import ru.radiationx.data.entity.domain.release.SeasonItem
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class SearchSeasonViewModel(
    private val argExtra: SearchValuesExtra,
    private val searchRepository: SearchRepository,
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter,
) : BaseSearchValuesViewModel(argExtra) {

    private val currentSeasons = mutableListOf<SeasonItem>()

    init {
        viewModelScope.launch {
            coRunCatching {
                searchRepository.getSeasons()
            }.onSuccess {
                currentSeasons.clear()
                currentSeasons.addAll(it)
                currentValues.clear()
                currentValues.addAll(it.map { it.value })
                valuesData.value = it.map { it.title }
                updateChecked()
                updateSelected()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    override fun applyValues() {
        guidedRouter.close()
        val newSeasons = currentSeasons.filter { item ->
            checkedValues.contains(item.value)
        }.toSet()
        searchController.seasonsEvent.emit(newSeasons)
    }
}