package ru.radiationx.anilibria.screen.suggestions

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.domain.search.Suggestions
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.controllers.loadersearch.SearchLoader
import ru.radiationx.shared_app.controllers.loadersearch.SearchQuery
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SuggestionsResultViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val cardRouter: LibriaCardRouter,
    private val suggestionsController: SuggestionsController,
) : LifecycleViewModel() {

    private val searchLoader = SearchLoader<Query, Suggestions>(viewModelScope) {
        searchRepository.fastSearch(it.query)
    }

    val progressState = MutableStateFlow(false)
    val resultData = MutableStateFlow<List<LibriaCard>>(emptyList())

    init {
        searchLoader
            .observeState {
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
            LibriaCard(
                it.names.getOrNull(0).orEmpty(),
                it.names.getOrNull(1).orEmpty(),
                it.poster.orEmpty(),
                LibriaCard.Type.Release(it.id)
            )
        }
    }

    private data class Query(val query: String) : SearchQuery {
        override fun isEmpty(): Boolean {
            return query.length < 3
        }
    }
}