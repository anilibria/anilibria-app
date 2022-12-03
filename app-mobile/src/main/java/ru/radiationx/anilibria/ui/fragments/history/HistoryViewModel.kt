package ru.radiationx.anilibria.ui.fragments.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
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
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor

/**
 * Created by radiationx on 18.02.18.
 */
@InjectConstructor
class HistoryViewModel(
    private val router: Router,
    private val historyRepository: HistoryRepository,
    private val historyAnalytics: HistoryAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    private val shortcutHelper: ShortcutHelper,
    private val systemUtils: SystemUtils
) : ViewModel() {

    private val currentReleases = mutableListOf<Release>()
    private val _state = MutableStateFlow(HistoryScreenState())
    val state = _state.asStateFlow()

    private var isSearchEnabled: Boolean = false
    private var currentQuery: String = ""

    private val updates = emptyMap<ReleaseId, ReleaseUpdate>()

    init {
        observeReleases()
    }

    fun onBackPressed() {
        router.exit()
    }

    private fun observeReleases() {
        historyRepository
            .observeReleases()
            .onEach { releases ->
                currentReleases.clear()
                currentReleases.addAll(releases)

                _state.update {
                    it.copy(items = currentReleases.map { it.toState(updates) })
                }

                updateSearchState()
            }
            .launchIn(viewModelScope)
    }

    private fun updateSearchState() {
        isSearchEnabled = currentQuery.isNotEmpty()
        val searchItes = if (currentQuery.isNotEmpty()) {
            currentReleases.filter {
                it.title.orEmpty().contains(currentQuery, true)
                        || it.titleEng.orEmpty().contains(currentQuery, true)
            }
        } else {
            emptyList()
        }
        _state.update {
            it.copy(searchItems = searchItes.map { it.toState(updates) })
        }
    }

    private fun findRelease(id: ReleaseId): Release? {
        return currentReleases.find { it.id == id }
    }

    fun localSearch(query: String) {
        currentQuery = query
        updateSearchState()
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        if (isSearchEnabled) {
            historyAnalytics.searchReleaseClick()
        } else {
            historyAnalytics.releaseClick()
        }
        releaseAnalytics.open(AnalyticsConstants.screen_history, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onDeleteClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        historyAnalytics.releaseDeleteClick()
        historyRepository.removeRelease(releaseItem.id)
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