package ru.radiationx.anilibria.screen.suggestions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.common.LibriaCardRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.domain.search.SuggestionItem
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.shared.ktx.coRunCatching
import toothpick.InjectConstructor

@InjectConstructor
class SuggestionsResultViewModel(
    private val searchRepository: SearchRepository,
    private val cardRouter: LibriaCardRouter,
    private val suggestionsController: SuggestionsController
) : LifecycleViewModel() {

    private var currentQuery = ""
    private var queryRelay = MutableSharedFlow<String>()

    val progressState = MutableLiveData<Boolean>()
    val resultData = MutableLiveData<List<LibriaCard>>()

    override fun onColdCreate() {
        super.onColdCreate()

        queryRelay
            .debounce(350L)
            .distinctUntilChanged()
            .onEach {
                if (it.length < 3) {
                    showItems(emptyList(), it, false)
                }
            }
            .filter { it.length >= 3 }
            .onEach { progressState.value = true }
            .mapLatest { query ->
                coRunCatching {
                    searchRepository.fastSearch(query)
                }.getOrNull() ?: emptyList()
            }
            .onEach {
                showItems(it, currentQuery, true)
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        viewModelScope.launch {
            currentQuery = query
            queryRelay.emit(currentQuery)
        }
    }

    fun onCardClick(item: LibriaCard) {
        cardRouter.navigate(item)
    }

    private fun showItems(items: List<SuggestionItem>, query: String, validQuery: Boolean) {
        val result = SuggestionsController.SearchResult(items, query, validQuery)
        suggestionsController.resultEvent.emit(result)
        progressState.value = false
        resultData.value = items.map {
            LibriaCard(
                it.names.getOrNull(0).orEmpty(),
                it.names.getOrNull(1).orEmpty(),
                it.poster.orEmpty(),
                LibriaCard.Type.Release(it.id)
            )
        }
    }
}