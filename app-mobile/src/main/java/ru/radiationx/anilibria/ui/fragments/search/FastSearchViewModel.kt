package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.SuggestionItemState
import ru.radiationx.anilibria.model.SuggestionLocalItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.domain.search.SuggestionItem
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.net.URLEncoder

@InjectConstructor
class FastSearchViewModel(
    private val searchRepository: SearchRepository,
    private val router: Router,
    private val systemUtils: SystemUtils,
    private val catalogAnalytics: CatalogAnalytics,
    private val fastSearchAnalytics: FastSearchAnalytics,
    private val releaseAnalytics: ReleaseAnalytics
) : ViewModel() {

    companion object {
        private const val ITEM_ID_SEARCH = -100
        private const val ITEM_ID_GOOGLE = -200
    }

    private val _state = MutableStateFlow(FastSearchScreenState())
    val state = _state.asStateFlow()

    private var currentQuery = ""
    private var queryRelay = MutableSharedFlow<String>()
    private var currentSuggestions = mutableListOf<SuggestionItem>()

    init {
        queryRelay
            .debounce(350L)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length >= 3) {
                    _state.update {
                        it.copy(loading = true)
                    }
                } else {
                    showItems(emptyList(), query, false)
                }
            }
            .filter { it.length >= 3 }
            .mapLatest { query ->
                runCatching {
                    searchRepository.fastSearch(query)
                }.getOrNull() ?: emptyList()
            }
            .onEach {
                showItems(it, currentQuery)
            }
            .launchIn(viewModelScope)
    }

    fun onClose() {
        currentQuery = ""
        showItems(emptyList(), currentQuery)
    }

    private fun showItems(items: List<SuggestionItem>, query: String, appendEmpty: Boolean = true) {
        currentSuggestions.clear()
        currentSuggestions.addAll(items)

        val isNotFound = appendEmpty && currentSuggestions.isEmpty() && query.isNotEmpty()
        val stateItems = currentSuggestions.map { it.toState(query) }
        val localItems = if (isNotFound) {
            listOf(
                SuggestionLocalItemState(
                    id = ITEM_ID_SEARCH,
                    icRes = R.drawable.ic_toolbar_search,
                    title = "Искать по жанрам и годам"
                ),
                SuggestionLocalItemState(
                    id = ITEM_ID_GOOGLE,
                    icRes = R.drawable.ic_google,
                    title = "Найти в гугле \"$query\""
                )
            )
        } else {
            emptyList()
        }

        _state.update {
            it.copy(
                loading = false,
                localItems = localItems,
                items = stateItems
            )
        }
    }

    fun onQueryChange(query: String) {
        viewModelScope.launch {
            currentQuery = query
            queryRelay.emit(currentQuery)
        }
    }

    fun onItemClick(item: SuggestionItemState) {
        val suggestionItem = currentSuggestions.find { it.id == item.id } ?: return
        fastSearchAnalytics.releaseClick()
        releaseAnalytics.open(AnalyticsConstants.screen_fast_search, suggestionItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(suggestionItem.id, suggestionItem.code))
    }

    fun onLocalItemClick(item: SuggestionLocalItemState) {
        when (item.id) {
            ITEM_ID_GOOGLE -> {
                fastSearchAnalytics.searchGoogleClick()
                val urlQuery = URLEncoder.encode("anilibria $currentQuery", "utf-8")
                systemUtils.externalLink("https://www.google.com/search?q=$urlQuery")
            }
            ITEM_ID_SEARCH -> {
                catalogAnalytics.open(AnalyticsConstants.screen_fast_search)
                fastSearchAnalytics.catalogClick()
                router.navigateTo(Screens.Catalog())
            }
        }
    }
}