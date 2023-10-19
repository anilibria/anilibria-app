package ru.radiationx.anilibria.ui.fragments.youtube

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.PageLoadParams
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.YoutubeAnalytics
import ru.radiationx.data.analytics.features.YoutubeVideosAnalytics
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.data.repository.YoutubeRepository
import ru.radiationx.shared_app.common.SystemUtils
import toothpick.InjectConstructor

@InjectConstructor
class YoutubeViewModel(
    private val youtubeRepository: YoutubeRepository,
    private val errorHandler: IErrorHandler,
    private val systemUtils: SystemUtils,
    private val youtubeAnalytics: YoutubeAnalytics,
    private val youtubeVideosAnalytics: YoutubeVideosAnalytics,
) : ViewModel() {

    private val loadingController = DataLoadingController(viewModelScope) {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }

    private val _state = MutableStateFlow(YoutubeScreenState())
    val state = _state.asStateFlow()

    private var currentRawItems = mutableListOf<YoutubeItem>()

    private var lastLoadedPage: Int? = null

    init {
        loadingController
            .observeState()
            .onEach { loadingState ->
                _state.update {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)
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
        youtubeAnalytics.openVideo(AnalyticsConstants.screen_youtube, rawItem.id.id, rawItem.vid)
        systemUtils.externalLink(rawItem.link)
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