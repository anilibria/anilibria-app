package ru.radiationx.anilibria.screen.suggestions

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.CardsDataConverter
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.domain.search.Suggestions
import ru.radiationx.data.repository.ReleaseRepository
import ru.radiationx.shared_app.controllers.loadersearch.SearchLoader
import ru.radiationx.shared_app.controllers.loadersearch.SearchQuery
import ru.radiationx.shared_app.controllers.loadersingle.mapData
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SuggestionsResultViewModel @Inject constructor(
    private val releaseRepository: ReleaseRepository,
    private val cardRouter: LibriaCardRouter,
    private val suggestionsController: SuggestionsController,
    private val cardsDataConverter: CardsDataConverter
) : LifecycleViewModel() {

    private val searchLoader = SearchLoader<Query, Suggestions>(viewModelScope) {
        releaseRepository.search(it.query)
    }

    val progressState = MutableStateFlow(false)
    val resultData = MutableStateFlow<List<LibriaCard>>(emptyList())

    init {
        searchLoader
            .observeState()
            .mapData {
                val query = Query(it.query)
                SuggestionsController.SearchResult(it.items, query.query, !query.isEmpty())
            }
            .onEach {
                progressState.value = it.loading
                val result = it.data ?: SuggestionsController.SearchResult(emptyList(), "", false)
                showItems(result)
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        searchLoader.onNewQuery(Query(query))
    }

    fun onCardClick(item: LibriaCard) {
        cardRouter.navigate(item)
    }

    private fun showItems(result: SuggestionsController.SearchResult) {
        suggestionsController.resultEvent.emit(result)
        resultData.value = result.items.map {
            cardsDataConverter.toCard(it)
        }
    }

    private data class Query(val query: String) : SearchQuery {
        override fun isEmpty(): Boolean {
            return query.length < 3
        }
    }
}