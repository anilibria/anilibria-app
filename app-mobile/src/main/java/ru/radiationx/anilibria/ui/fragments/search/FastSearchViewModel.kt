package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.SuggestionItemState
import ru.radiationx.anilibria.model.SuggestionLocalItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.domain.search.Suggestions
import ru.radiationx.data.repository.SearchRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.controllers.loadersearch.SearchLoader
import ru.radiationx.shared_app.controllers.loadersearch.SearchQuery
import ru.radiationx.shared_app.controllers.loadersingle.mapData
import java.net.URLEncoder
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FastSearchViewModel @Inject constructor(
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

    private val searchLoader = SearchLoader<Query, Suggestions>(viewModelScope) {
        searchRepository.fastSearch(it.query)
    }

    private val _state = MutableStateFlow(FastSearchScreenState())
    val state = _state.asStateFlow()

    init {
        searchLoader
            .observeState()
            .map { state ->
                state.mapData { data ->
                    val localItems = if (data.items.isEmpty()) {
                        createLocalItems(data.query)
                    } else {
                        emptyList()
                    }
                    FastSearchDataState(
                        localItems,
                        data.items.map { it.toState(data.query) }
                    )
                }
            }
            .onEach { state ->
                _state.update {
                    it.copy(loaderState = state)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        searchLoader.onNewQuery(Query(query))
    }

    fun refresh() {
        searchLoader.refresh()
    }

    fun onItemClick(item: SuggestionItemState) {
        fastSearchAnalytics.releaseClick()
        releaseAnalytics.open(AnalyticsConstants.screen_fast_search, item.id.id)
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code))
    }

    fun onLocalItemClick(item: SuggestionLocalItemState) {
        when (item.id) {
            ITEM_ID_GOOGLE -> {
                fastSearchAnalytics.searchGoogleClick()
                val urlQuery =
                    URLEncoder.encode("anilibria ${searchLoader.getQuery()?.query}", "utf-8")
                systemUtils.externalLink("https://www.google.com/search?q=$urlQuery")
            }

            ITEM_ID_SEARCH -> {
                catalogAnalytics.open(AnalyticsConstants.screen_fast_search)
                fastSearchAnalytics.catalogClick()
                router.navigateTo(Screens.Catalog())
            }
        }
    }

    private fun createLocalItems(query: String): List<SuggestionLocalItemState> = listOf(
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

    private data class Query(val query: String) : SearchQuery {
        override fun isEmpty(): Boolean {
            return query.length < 3
        }
    }
}