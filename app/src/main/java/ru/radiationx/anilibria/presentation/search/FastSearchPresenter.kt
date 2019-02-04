package ru.radiationx.anilibria.presentation.search

import android.os.Bundle
import com.arellomobile.mvp.InjectViewState
import com.jakewharton.rxrelay2.PublishRelay
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.entity.app.search.SuggestionItem
import ru.radiationx.anilibria.model.repository.SearchRepository
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.radiationx.anilibria.ui.navigation.AppRouter
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

@InjectViewState
class FastSearchPresenter(
        private val schedulers: SchedulersProvider,
        private val searchRepository: SearchRepository,
        private val router: AppRouter,
        private val errorHandler: IErrorHandler
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
                val urlQuery = URLEncoder.encode("anilibria ${item.query}", "utf-8");
                Utils.externalLink("https://www.google.com/search?q=$urlQuery")
            }
            ITEM_ID_SEARCH -> {
                router.navigateTo(Screens.ReleasesSearch())
            }
            else -> {
                (item as? SuggestionItem)?.also {
                    val args = Bundle()
                    args.putInt(ReleaseFragment.ARG_ID, it.id)
                    args.putString(ReleaseFragment.ARG_ID_CODE, it.code)
                    router.navigateTo(Screens.ReleaseDetails(args))
                }
            }
        }
    }
}