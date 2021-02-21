package ru.radiationx.anilibria.presentation.search

import com.jakewharton.rxrelay2.PublishRelay
import moxy.InjectViewState
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.entity.app.search.SearchItem
import ru.radiationx.data.entity.app.search.SuggestionItem
import ru.radiationx.data.repository.SearchRepository
import ru.terrakok.cicerone.Router
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
class FastSearchPresenter @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val searchRepository: SearchRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler,
        private val catalogAnalytics: CatalogAnalytics
) : BasePresenter<FastSearchView>(router) {

    companion object {
        private const val ITEM_ID_SEARCH = -100
        private const val ITEM_ID_GOOGLE = -200
    }

    private var currentQuery = ""
    private var queryRelay = PublishRelay.create<String>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        queryRelay
                .debounce(350L, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(schedulers.ui())
                .doOnNext {
                    if (it.length >= 3) {
                        viewState.setSearchProgress(true)
                    } else {
                        showItems(emptyList(), it, false)
                    }
                }
                .filter { it.length >= 3 }
                .switchMapSingle { query ->
                    searchRepository
                            .fastSearch(query)
                            .onErrorReturnItem(emptyList())
                }
                .observeOn(schedulers.ui())
                .subscribe({
                    showItems(it, currentQuery)
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    fun onClose() {
        currentQuery = ""
        showItems(emptyList(), currentQuery)
    }

    fun showItems(items: List<SearchItem>, query: String, appendEmpty: Boolean = true) {
        viewState.setSearchProgress(false)
        val resItems = mutableListOf<SearchItem>()
        resItems.addAll(items)
        if (appendEmpty && resItems.isEmpty() && query.isNotEmpty()) {
            resItems.add(SearchItem().apply {
                id = ITEM_ID_SEARCH
                icRes = R.drawable.ic_toolbar_search
                title = "Искать по жанрам и годам"
            })
            resItems.add(SearchItem().apply {
                id = ITEM_ID_GOOGLE
                icRes = R.drawable.ic_google
                title = "Найти в гугле \"$query\""
            })
        }
        resItems.forEach { it.query = query }
        viewState.showSearchItems(resItems)
    }

    fun onQueryChange(query: String) {
        currentQuery = query
        queryRelay.accept(currentQuery)
    }

    fun onItemClick(item: SearchItem) {
        when (item.id) {
            ITEM_ID_GOOGLE -> {
                val urlQuery = URLEncoder.encode("anilibria ${item.query}", "utf-8")
                Utils.externalLink("https://www.google.com/search?q=$urlQuery")
            }
            ITEM_ID_SEARCH -> {
                catalogAnalytics.open(AnalyticsConstants.screen_fast_search)
                router.navigateTo(Screens.ReleasesSearch())
            }
            else -> {
                (item as? SuggestionItem)?.also {
                    router.navigateTo(Screens.ReleaseDetails(it.id, it.code))
                }
            }
        }
    }
}