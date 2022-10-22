package ru.radiationx.anilibria.presentation.search

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.SuggestionItemState
import ru.radiationx.anilibria.model.SuggestionLocalItemState
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.data.repository.SearchRepository
import ru.terrakok.cicerone.Router
import java.net.URLEncoder
import javax.inject.Inject

@InjectViewState
class FastSearchPresenter @Inject constructor(
    private val schedulers: SchedulersProvider,
    private val searchRepository: SearchRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val catalogAnalytics: CatalogAnalytics,
    private val fastSearchAnalytics: FastSearchAnalytics,
    private val releaseAnalytics: ReleaseAnalytics
) : BasePresenter<FastSearchView>(router) {

    companion object {
        private const val ITEM_ID_SEARCH = -100
        private const val ITEM_ID_GOOGLE = -200
    }

    private val stateController = StateController(FastSearchScreenState())

    private var currentQuery = ""
    private var queryRelay = MutableSharedFlow<String>()
    private var currentSuggestions = mutableListOf<SuggestionItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)

        queryRelay
            .debounce(350L)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length >= 3) {
                    stateController.updateState {
                        it.copy(loading = true)
                    }
                } else {
                    showItems(emptyList(), query, false)
                }
            }
            .filter { it.length >= 3 }
            .map { query ->
                runCatching {
                    searchRepository.fastSearch(query)
                }.getOrNull() ?: emptyList()
            }
            .onEach {
                showItems(it, currentQuery)
            }
            .launchIn(presenterScope)
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

        stateController.updateState {
            it.copy(
                loading = false,
                localItems = localItems,
                items = stateItems
            )
        }
    }

    fun onQueryChange(query: String) {
        presenterScope.launch {
            currentQuery = query
            queryRelay.emit(currentQuery)
        }
    }

    fun onItemClick(item: SuggestionItemState) {
        val suggestionItem = currentSuggestions.find { it.id == item.id } ?: return
        fastSearchAnalytics.releaseClick()
        releaseAnalytics.open(AnalyticsConstants.screen_fast_search, suggestionItem.id)
        router.navigateTo(Screens.ReleaseDetails(suggestionItem.id, suggestionItem.code))
    }

    fun onLocalItemClick(item: SuggestionLocalItemState) {
        when (item.id) {
            ITEM_ID_GOOGLE -> {
                fastSearchAnalytics.searchGoogleClick()
                val urlQuery = URLEncoder.encode("anilibria $currentQuery", "utf-8")
                Utils.externalLink("https://www.google.com/search?q=$urlQuery")
            }
            ITEM_ID_SEARCH -> {
                catalogAnalytics.open(AnalyticsConstants.screen_fast_search)
                fastSearchAnalytics.catalogClick()
                router.navigateTo(Screens.ReleasesSearch())
            }
        }
    }
}