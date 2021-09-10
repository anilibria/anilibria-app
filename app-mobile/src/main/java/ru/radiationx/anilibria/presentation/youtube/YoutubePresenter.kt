package ru.radiationx.anilibria.presentation.youtube

import io.reactivex.disposables.Disposables
import moxy.InjectViewState
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.applyAction
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.presentation.Paginator
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

    private var dataDisposable = Disposables.disposed()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refresh()
    }

    private fun loadPage(page: Int) {
        if (!dataDisposable.isDisposed) {
            return
        }

        if (lastLoadedPage != page) {
            youtubeVideosAnalytics.loadPage(page)
            lastLoadedPage = page
        }

        val isFirstPage = page == START_PAGE
        val isEmptyData = currentState.data.data == null

        val action: ScreenStateAction<List<YoutubeItemState>> = when {
            isFirstPage && isEmptyData -> ScreenStateAction.EmptyLoading()
            isFirstPage && !isEmptyData -> ScreenStateAction.Refresh()
            else -> ScreenStateAction.MoreLoading()
        }
        updateStateByAction(action)

        dataDisposable = youtubeRepository
            .getYoutubeList(page)
            .subscribe({ items ->
                if (page == START_PAGE) {
                    currentRawItems.clear()
                }
                currentRawItems.addAll(items.data)

                val newItems = currentRawItems.map { item -> item.toState() }
                val action = ScreenStateAction.Data(newItems, !items.isEnd())
                updateStateByAction(action)
                currentPage = page
            }) { throwable ->
                if (page == Paginator.FIRST_PAGE) {
                    errorHandler.handle(throwable)
                }
                updateStateByAction(ScreenStateAction.Error(throwable))
            }
            .addToDisposable()
    }

    private fun updateState(block: (YoutubeScreenState) -> YoutubeScreenState) {
        currentState = block.invoke(currentState)
        viewState.showState(currentState)
    }

    private fun updateStateByAction(action: ScreenStateAction<List<YoutubeItemState>>) {
        updateState {
            it.copy(data = it.data.applyAction(action))
        }
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