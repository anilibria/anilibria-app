package ru.radiationx.anilibria.presentation.history

import moxy.InjectViewState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.ui.fragments.release.list.ReleaseScreenState
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.HistoryAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.app.release.ReleaseItem
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

    private val currentReleases = mutableListOf<ReleaseItem>()

    private var currentState = ReleaseScreenState()

    private fun updateState(block: (ReleaseScreenState) -> ReleaseScreenState) {
        currentState = block.invoke(currentState)
        viewState.showState(currentState)
    }

    private var isSearchEnabled: Boolean = false
    private var currentQuery: String = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        observeReleases()
    }

    private fun observeReleases() {
        historyRepository
            .observeReleases()
            .subscribe {
                currentReleases.clear()
                currentReleases.addAll(it)
                updateData()
            }
            .addToDisposable()
    }

    private fun updateData() {
        isSearchEnabled = currentQuery.isNotEmpty()
        val newReleases = if (currentQuery.isNotEmpty()) {
            val searchRes = currentReleases.filter {
                it.title.orEmpty().contains(currentQuery, true) || it.titleEng.orEmpty()
                    .contains(currentQuery, true)
            }
            searchRes
        } else {
            currentReleases
        }
        val newItems = newReleases.map { it.toState() }
        updateState { it.copy(items = newItems) }
    }

    private fun findRelease(id: Int): ReleaseItem? {
        return currentReleases.find { it.id == id }
    }

    fun localSearch(query: String) {
        currentQuery = query
        updateData()
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