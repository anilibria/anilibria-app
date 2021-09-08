package ru.radiationx.anilibria.presentation.youtube

import moxy.InjectViewState
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.youtube.YoutubeScreenState
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.YoutubeAnalytics
import ru.radiationx.data.analytics.features.YoutubeVideosAnalytics
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import ru.radiationx.data.repository.YoutubeRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class YoutubePresenter @Inject constructor(
    private val youtubeRepository: YoutubeRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val youtubeAnalytics: YoutubeAnalytics,
    private val youtubeVideosAnalytics: YoutubeVideosAnalytics
) : BasePresenter<YoutubeView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var currentRawItems = mutableListOf<YoutubeItem>()
    private var currentState = YoutubeScreenState()

    private var lastLoadedPage: Int? = null
    private var currentPage = START_PAGE

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refresh()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    private fun loadPage(page: Int) {
        if (lastLoadedPage != page) {
            youtubeVideosAnalytics.loadPage(page)
            lastLoadedPage = page
        }
        currentPage = page
        if (isFirstPage()) {
            updateState {
                it.copy(refreshing = true)
            }
        }
        youtubeRepository
            .getYoutubeList(page)
            .doFinally {
                updateState {
                    it.copy(refreshing = false)
                }
            }
            .subscribe({ items ->
                if (isFirstPage()) {
                    currentRawItems.clear()
                }
                currentRawItems.addAll(items.data)

                updateState {
                    it.copy(
                        items = items.data.map { item -> item.toState() },
                        hasMorePages = !items.isEnd()
                    )
                }
            }) {
                errorHandler.handle(it)
            }
            .addToDisposable()
    }

    private fun updateState(block: (YoutubeScreenState) -> YoutubeScreenState) {
        currentState = block.invoke(currentState)
        viewState.showState(currentState)
    }

    fun refresh() {
        loadPage(START_PAGE)
    }

    fun loadMore() {
        loadPage(currentPage + 1)
    }

    fun onItemClick(item: YoutubeItemState) {
        val rawItem = currentRawItems.firstOrNull { it.id == item.id } ?: return
        youtubeVideosAnalytics.videoClick()
        youtubeAnalytics.openVideo(AnalyticsConstants.screen_youtube, rawItem.id, rawItem.vid)
        Utils.externalLink(rawItem.link)
    }


}