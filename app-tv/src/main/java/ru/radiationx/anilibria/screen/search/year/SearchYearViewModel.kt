package ru.radiationx.anilibria.screen.search.year

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.search.BaseSearchValuesViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import ru.radiationx.anilibria.screen.search.SearchValuesExtra
import ru.radiationx.data.entity.domain.release.YearItem
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class SearchYearViewModel(
    argExtra: SearchValuesExtra,
    private val searchRepository: SearchRepository,
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter,
) : BaseSearchValuesViewModel(argExtra) {

    private val currentYears = mutableListOf<YearItem>()

    init {
        searchRepository
            .observeYears()
            .onEach { years ->
                currentYears.clear()
                currentYears.addAll(years)
                currentValues.clear()
                currentValues.addAll(years.map { it.value })
                valuesData.value = years.map { it.title }
                progressState.value = false
                updateChecked()
                updateSelected()
            }
            .launchIn(viewModelScope)


        viewModelScope.launch {
            progressState.value = true
            coRunCatching {
                searchRepository.getYears()
            }.onFailure {
                Timber.e(it)
            }
            progressState.value = false
        }
    }

    override fun applyValues() {
        guidedRouter.close()
        val newYears = currentYears.filter { item ->
            checkedValues.contains(item.value)
        }.toSet()
        searchController.yearsEvent.emit(newYears)
    }
}