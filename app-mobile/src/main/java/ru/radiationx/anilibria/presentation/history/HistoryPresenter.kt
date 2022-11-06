package ru.radiationx.anilibria.presentation.history

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moxy.InjectViewState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.StateController
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.ui.fragments.history.HistoryScreenState
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.HistoryAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.release.ReleaseUpdate
import ru.radiationx.data.repository.HistoryRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by radiationx on 18.02.18.
 */
@InjectViewState
class HistoryPresenter @Inject constructor(
    private val router: Router,
    private val historyRepository: HistoryRepository,
    private val historyAnalytics: HistoryAnalytics,
    private val releaseAnalytics: ReleaseAnalytics
) : BasePresenter<HistoryView>(router) {

    private val currentReleases = mutableListOf<Release>()
    private val stateController = StateController(HistoryScreenState())

    private var isSearchEnabled: Boolean = false
    private var currentQuery: String = ""

    private val updates = emptyMap<Int, ReleaseUpdate>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)
        observeReleases()
    }

    private fun observeReleases() {
        historyRepository
            .observeReleases()
            .onEach { releases ->
                currentReleases.clear()
                currentReleases.addAll(releases)

                stateController.updateState {
                    it.copy(items = currentReleases.map { it.toState(updates) })
                }

                updateSearchState()
            }
            .launchIn(presenterScope)
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
        stateController.updateState {
            it.copy(searchItems = searchItes.map { it.toState(updates) })
        }
    }

    private fun findRelease(id: Int): Release? {
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
        releaseAnalytics.open(AnalyticsConstants.screen_history, releaseItem.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onDeleteClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        historyAnalytics.releaseDeleteClick()
        historyRepository.removeRelease(releaseItem.id)
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_history, releaseItem.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.shareText(releaseItem.link.orEmpty())
        releaseAnalytics.share(AnalyticsConstants.screen_history, releaseItem.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        ShortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_history, releaseItem.id)
    }

    fun onSearchClick() {
        historyAnalytics.searchClick()
    }
}