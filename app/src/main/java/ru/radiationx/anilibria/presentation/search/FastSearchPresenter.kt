package ru.radiationx.anilibria.presentation.search

import com.arellomobile.mvp.InjectViewState
import com.jakewharton.rxrelay2.PublishRelay
import ru.radiationx.anilibria.model.repository.SearchRepository
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router
import java.util.concurrent.TimeUnit

@InjectViewState
class FastSearchPresenter(
        private val schedulers: SchedulersProvider,
        private val searchRepository: SearchRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler
) : BasePresenter<FastSearchView>(router) {

    private var currentQuery = ""
    private var queryRelay = PublishRelay.create<String>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        queryRelay
                .debounce(350L, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(schedulers.ui())
                .doOnNext {
                    viewState.setSearchProgress(true)
                }
                .switchMapSingle { query ->
                    searchRepository
                            .fastSearch(query)
                            .onErrorReturnItem(emptyList())
                }
                .observeOn(schedulers.ui())
                .subscribe({
                    viewState.setSearchProgress(false)
                    viewState.showSearchItems(it, currentQuery)
                }, {
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }

    fun onClose() {
        currentQuery = ""
        viewState.showSearchItems(emptyList(), currentQuery)
    }

    fun onQueryChange(query: String) {
        currentQuery = query
        queryRelay.accept(currentQuery)
    }
}