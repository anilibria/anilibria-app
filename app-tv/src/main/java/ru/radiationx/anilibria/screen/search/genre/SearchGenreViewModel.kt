package ru.radiationx.anilibria.screen.search.genre

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.search.BaseSearchValuesViewModel
import ru.radiationx.anilibria.screen.search.SearchController
import ru.radiationx.anilibria.screen.search.SearchValuesExtra
import ru.radiationx.data.entity.domain.release.GenreItem
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.shared.ktx.coRunCatching
import timber.log.Timber
import toothpick.InjectConstructor

@InjectConstructor
class SearchGenreViewModel(
    private val argExtra: SearchValuesExtra,
    private val searchRepository: SearchRepository,
    private val searchController: SearchController,
    private val guidedRouter: GuidedRouter
) : BaseSearchValuesViewModel(argExtra) {

    private val currentGenres = mutableListOf<GenreItem>()

    override fun onColdCreate() {
        super.onColdCreate()
        searchRepository
            .observeGenres()
            .onEach {
                currentGenres.clear()
                currentGenres.addAll(it)
                currentValues.clear()
                currentValues.addAll(it.map { it.value })
                valuesData.value = it.map { it.title }
                progressState.value = false
                updateChecked()
                updateSelected()
            }
            .launchIn(viewModelScope)
    }

    override fun onCreate() {
        super.onCreate()
        viewModelScope.launch {
            progressState.value = true
            coRunCatching {
                searchRepository.getGenres()
            }.onFailure {
                Timber.e(it)
            }
            progressState.value = false
        }
    }

    override fun applyValues() {
        viewModelScope.launch {
            guidedRouter.close()
            val newGenres = currentGenres.filterIndexed { index, item ->
                checkedValues.contains(item.value)
            }.toSet()
            searchController.genresEvent.emit(newGenres)
        }
    }
}