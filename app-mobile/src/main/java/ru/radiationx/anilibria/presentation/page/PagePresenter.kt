package ru.radiationx.anilibria.presentation.page

import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.features.PageAnalytics
import ru.radiationx.data.repository.PageRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
@InjectViewState
class PagePresenter @Inject constructor(
        private val pageRepository: PageRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler,
        private val pageAnalytics: PageAnalytics
) : BasePresenter<PageView>(router) {

    var pagePath: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        pagePath?.also {
            loadPage(it)
        }
    }

    private fun loadPage(pagePath: String) {
        viewState.setRefreshing(true)
        pageRepository
                .getPage(pagePath)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ page ->
                    viewState.showPage(page)
                }, {
                    pageAnalytics.error(it)
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }
}