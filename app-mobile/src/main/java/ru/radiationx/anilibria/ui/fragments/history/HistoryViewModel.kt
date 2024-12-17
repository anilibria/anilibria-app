package ru.radiationx.anilibria.ui.fragments.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.HistoryAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.repository.HistoryRepository
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
class HistoryViewModel @Inject constructor(
    private val router: Router,
    private val historyRepository: HistoryRepository,
    private val historyAnalytics: HistoryAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val shortcutHelper: ShortcutHelper,
    private val systemUtils: SystemUtils,
) : ViewModel() {

    private fun pageToCount(page: Int) = page * 50

    private val pageLoader = PageLoader(viewModelScope) {
        val count = pageToCount(it.page)
        val history = historyRepository.getReleases(count)
        it.toDataAction(history.hasMore) { history.items }
    }

    private val _state = MutableStateFlow(HistoryScreenState())
    val state = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    private val updates = emptyMap<ReleaseId, ReleaseUpdate>()

    init {
        pageLoader
            .observeState { data ->
                data.map { it.toState(updates) }
            }
            .onEach { loadingState ->
                _state.update {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)

        pageLoader
            .observePage()
            .flatMapLatest { page ->
                historyRepository.observeReleases(pageToCount(page))
            }
            .onEach { history ->
                pageLoader.modifyData(history.hasMore) { history.items }
            }
            .launchIn(viewModelScope)

        combine(
            queryFlow,
            pageLoader.observeState().map { it.data.orEmpty() }.distinctUntilChanged()
        ) { query, releases ->
            if (query.isEmpty()) {
                return@combine emptyList()
            }
            releases.filter {
                it.title.orEmpty().contains(query, true)
                        || it.titleEng.orEmpty().contains(query, true)
            }
        }
            .distinctUntilChanged()
            .onEach { searchItems ->
                _state.update { state ->
                    state.copy(searchItems = searchItems.map { it.toState(updates) })
                }
            }
            .launchIn(viewModelScope)

        pageLoader.refresh()
    }

    fun onBackPressed() {
        router.exit()
    }

    fun refresh() {
        pageLoader.refresh()
    }

    fun loadMore() {
        pageLoader.loadMore()
    }

    private fun findRelease(id: ReleaseId): Release? {
        return pageLoader.getData()?.find { it.id == id }
    }

    fun localSearch(query: String) {
        queryFlow.value = query
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        if (queryFlow.value.isNotEmpty()) {
            historyAnalytics.searchReleaseClick()
        } else {
            historyAnalytics.releaseClick()
        }
        releaseAnalytics.open(AnalyticsConstants.screen_history, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onDeleteClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        viewModelScope.launch {
            historyAnalytics.releaseDeleteClick()
            historyRepository.removeRelease(releaseItem.id)
        }
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_history, releaseItem.id.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.shareText(releaseItem.link.orEmpty())
        releaseAnalytics.share(AnalyticsConstants.screen_history, releaseItem.id.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        shortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_history, releaseItem.id.id)
    }

    fun onSearchClick() {
        historyAnalytics.searchClick()
    }
}