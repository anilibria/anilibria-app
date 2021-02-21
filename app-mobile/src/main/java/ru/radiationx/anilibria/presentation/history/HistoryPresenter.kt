package ru.radiationx.anilibria.presentation.history

import moxy.InjectViewState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
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

    private var isSearchEnabled: Boolean = false

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
                viewState.showReleases(it)
            }
            .addToDisposable()
    }

    fun localSearch(query: String) {
        isSearchEnabled = query.isNotEmpty()
        if (query.isNotEmpty()) {
            val searchRes = currentReleases.filter {
                it.title.orEmpty().contains(query, true) || it.titleEng.orEmpty()
                    .contains(query, true)
            }
            viewState.showReleases(searchRes)
        } else {
            viewState.showReleases(currentReleases)
        }
    }

    fun onItemClick(item: ReleaseItem) {
        if (isSearchEnabled) {
            historyAnalytics.searchReleaseClick()
        } else {
            historyAnalytics.releaseClick()
        }
        releaseAnalytics.open(AnalyticsConstants.screen_history, item.id)
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code, item))
    }

    fun onDeleteClick(item: ReleaseItem) {
        historyAnalytics.releaseDeleteClick()
        historyRepository.removeRelease(item.id)
    }

    fun onCopyClick(item:ReleaseItem){
        releaseAnalytics.copyLink(AnalyticsConstants.screen_history, item.id)
    }

    fun onShareClick(item: ReleaseItem){
        releaseAnalytics.share(AnalyticsConstants.screen_history, item.id)
    }

    fun onShortcutClick(item: ReleaseItem){
        releaseAnalytics.shortcut(AnalyticsConstants.screen_history, item.id)
    }

    fun onSearchClick() {
        historyAnalytics.searchClick()
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}