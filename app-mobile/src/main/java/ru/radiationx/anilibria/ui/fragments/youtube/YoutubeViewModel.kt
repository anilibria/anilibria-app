package ru.radiationx.anilibria.ui.fragments.youtube

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.YoutubeAnalytics
import ru.radiationx.data.analytics.features.YoutubeVideosAnalytics
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.data.repository.YoutubeRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderAction
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderParams
import ru.radiationx.shared_app.controllers.loaderpage.mapData
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import toothpick.InjectConstructor

@InjectConstructor
class YoutubeViewModel(
    private val youtubeRepository: YoutubeRepository,
    private val errorHandler: IErrorHandler,
    private val systemUtils: SystemUtils,
    private val youtubeAnalytics: YoutubeAnalytics,
    private val youtubeVideosAnalytics: YoutubeVideosAnalytics,
) : ViewModel() {

    private val pageLoader = PageLoader<Unit, List<YoutubeItem>>(viewModelScope) {
        submitPageAnalytics(page)
        getDataSource(this)
    }

    private val _state = MutableStateFlow(YoutubeScreenState())
    val state = _state.asStateFlow()

    private var lastLoadedPage: Int? = null

    init {
        pageLoader
            .observeState()
            .map { loadingState ->
                loadingState.mapData { items ->
                    items.map { it.toState() }
                }
            }
            .onEach { loadingState ->
                _state.update {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        pageLoader.refresh(Unit)
    }

    fun loadMore() {
        pageLoader.loadMore()
    }

    fun onItemClick(item: YoutubeItemState) {
        val rawItem = pageLoader.getData()?.firstOrNull { it.id == item.id } ?: return
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

    private suspend fun getDataSource(params: PageLoaderParams<List<YoutubeItem>>): PageLoaderAction.Data<List<YoutubeItem>> {
        return try {
            val result = youtubeRepository.getYoutubeList(params.page)
            params.toDataAction(!result.isEnd()) {
                it.orEmpty() + result.data
            }
        } catch (ex: Throwable) {
            if (params.isFirstPage) {
                errorHandler.handle(ex)
            }
            throw ex
        }
    }

}