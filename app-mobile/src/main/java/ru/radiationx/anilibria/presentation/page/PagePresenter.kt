package ru.radiationx.anilibria.presentation.page

import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            viewState.setRefreshing(true)
            runCatching {
                pageRepository.getPage(pagePath)
            }.onSuccess {
                viewState.showPage(it)
            }.onFailure {
                pageAnalytics.error(it)
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }
}