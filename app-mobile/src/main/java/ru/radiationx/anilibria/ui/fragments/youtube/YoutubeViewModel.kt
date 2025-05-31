package ru.radiationx.anilibria.ui.fragments.youtube

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.anilibria.model.YoutubeItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.YoutubeAnalytics
import ru.radiationx.data.analytics.features.YoutubeVideosAnalytics
import ru.radiationx.data.entity.domain.types.YoutubeId
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.data.repository.YoutubeRepository
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderAction
import ru.radiationx.shared_app.controllers.loaderpage.PageLoaderParams
import ru.radiationx.shared_app.controllers.loaderpage.mapData
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import javax.inject.Inject

class YoutubeViewModel @Inject constructor(
    private val youtubeRepository: YoutubeRepository,
    private val errorHandler: IErrorHandler,
    private val systemUtils: SystemUtils,
    private val youtubeAnalytics: YoutubeAnalytics,
    private val youtubeVideosAnalytics: YoutubeVideosAnalytics,
) : ViewModel() {

    private val pageLoader = PageLoader(viewModelScope) {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }

    private val _state = MutableStateFlow(YoutubeScreenState())
    val state = _state.asStateFlow()

    private var lastLoadedPage: Int? = null

    init {
        pageLoader
            .observeState()
            .mapData { items ->
                items.map { it.toState() }
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
        pageLoader.refresh()
    }

    fun loadMore() {
        pageLoader.loadMore()
    }

    fun onCopyClick(item: YoutubeItemState) {
        val rawItem = findItem(item.id) ?: return
        systemUtils.copy(rawItem.link)
    }

    fun onShareClick(item: YoutubeItemState) {
        val rawItem = findItem(item.id) ?: return
        systemUtils.share(rawItem.link)
    }

    fun onItemClick(item: YoutubeItemState) {
        val rawItem = findItem(item.id) ?: return
        youtubeVideosAnalytics.videoClick()
        youtubeAnalytics.openVideo(AnalyticsConstants.screen_youtube, rawItem.id.id, rawItem.vid)
        systemUtils.open(rawItem.link)
    }

    private fun findItem(id: YoutubeId): YoutubeItem? {
        return pageLoader.getData()?.firstOrNull { it.id == id }
    }

    private fun submitPageAnalytics(page: Int) {
        if (lastLoadedPage != page) {
            youtubeVideosAnalytics.loadPage(page)
            lastLoadedPage = page
        }
    }

    private suspend fun getDataSource(params: PageLoaderParams<List<YoutubeItem>>): PageLoaderAction.Data<List<YoutubeItem>> {
        return coRunCatching {
            val result = youtubeRepository.getYoutubeList(params.page)
            params.toDataAction(!result.isEnd()) {
                it.orEmpty() + result.data
            }
        }.onFailure {
            if (params.isFirstPage) {
                errorHandler.handle(it)
            }
        }.getOrThrow()
    }

}