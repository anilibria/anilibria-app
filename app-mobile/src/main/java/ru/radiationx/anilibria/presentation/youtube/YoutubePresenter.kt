package ru.radiationx.anilibria.presentation.youtube

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moxy.InjectViewState
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.PageLoadParams
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.youtube.YoutubeScreenState
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.YoutubeAnalytics
import ru.radiationx.data.analytics.features.YoutubeVideosAnalytics
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
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

    private val loadingController = DataLoadingController(presenterScope) {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }

    private val stateController = StateController(YoutubeScreenState())

    private var currentRawItems = mutableListOf<YoutubeItem>()

    private var lastLoadedPage: Int? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)

        loadingController
            .observeState()
            .onEach { loadingState ->
                stateController.updateState {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(presenterScope)
        loadingController.refresh()
    }

    fun refresh() {
        loadingController.refresh()
    }

    fun loadMore() {
        loadingController.loadMore()
    }

    fun onItemClick(item: YoutubeItemState) {
        val rawItem = currentRawItems.firstOrNull { it.id == item.id } ?: return
        youtubeVideosAnalytics.videoClick()
        youtubeAnalytics.openVideo(AnalyticsConstants.screen_youtube, rawItem.id, rawItem.vid)
        Utils.externalLink(rawItem.link)
    }

    private fun submitPageAnalytics(page: Int) {
        if (lastLoadedPage != page) {
            youtubeVideosAnalytics.loadPage(page)
            lastLoadedPage = page
        }
    }

    private suspend fun getDataSource(params: PageLoadParams): ScreenStateAction.Data<List<YoutubeItemState>> {
        return try {
            youtubeRepository
                .getYoutubeList(params.page)
                .let { paginated ->
                    if (params.isFirstPage) {
                        currentRawItems.clear()
                    }
                    currentRawItems.addAll(paginated.data)

                    val newItems = currentRawItems.map { item -> item.toState() }
                    ScreenStateAction.Data(newItems, !paginated.isEnd())
                }
        } catch (ex: Throwable) {
            if (params.isFirstPage) {
                errorHandler.handle(ex)
            }
            throw ex
        }
    }

}