package ru.radiationx.anilibria.presentation.youtube

import moxy.InjectViewState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.YoutubeAnalytics
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.data.repository.YoutubeRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class YoutubePresenter @Inject constructor(
        private val youtubeRepository: YoutubeRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler,
        private val youtubeAnalytics: YoutubeAnalytics
) : BasePresenter<YoutubeView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refresh()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    private fun loadPage(page: Int) {
        currentPage = page
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        youtubeRepository
                .getYoutubeList(page)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ items ->
                    viewState.setEndless(!items.isEnd())
                    showData(items.data)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun showData(data: List<YoutubeItem>) {
        if (isFirstPage()) {
            viewState.showItems(data)
        } else {
            viewState.insertMore(data)
        }
    }

    fun refresh() {
        loadPage(START_PAGE)
    }

    fun loadMore() {
        loadPage(currentPage + 1)
    }

    fun onItemClick(item: YoutubeItem) {
        youtubeAnalytics.openVideo(AnalyticsConstants.screen_youtube, item.id, item.vid)
        Utils.externalLink(item.link)
    }

    fun onItemLongClick(item: YoutubeItem): Boolean {
        return false
    }
}