package ru.radiationx.anilibria.presentation.page

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.data.repository.PageRepository
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
@InjectViewState
class PagePresenter @Inject constructor(
        private val pageRepository: PageRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler
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
                    errorHandler.handle(it)
                })
                .addToDisposable()
    }
}