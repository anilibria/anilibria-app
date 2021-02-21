package ru.radiationx.anilibria.presentation.release.list

import moxy.InjectViewState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.FastSearchAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.vital.VitalItem
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.data.repository.VitalRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/* Created by radiationx on 05.11.17. */

@InjectViewState
class ReleasesPresenter @Inject constructor(
        private val releaseInteractor: ReleaseInteractor,
        private val vitalRepository: VitalRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler,
        private val releaseUpdateHolder: ReleaseUpdateHolder,
        private val fastSearchAnalytics: FastSearchAnalytics,
        private val releaseAnalytics: ReleaseAnalytics
) : BasePresenter<ReleasesView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE
    private val currentItems = mutableListOf<ReleaseItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refreshReleases()
        loadVital()

        releaseUpdateHolder
                .observeEpisodes()
                .subscribe { data ->
                    val itemsNeedUpdate = mutableListOf<ReleaseItem>()
                    currentItems.forEach { item ->
                        data.firstOrNull { it.id == item.id }?.also { updItem ->
                            val isNew = item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                            if (item.isNew != isNew) {
                                item.isNew = isNew
                                itemsNeedUpdate.add(item)
                            }
                        }
                    }

                    viewState.updateReleases(itemsNeedUpdate)
                }
                .addToDisposable()
    }

    private fun loadVital() {
        vitalRepository
                .observeByRule(VitalItem.Rule.RELEASE_LIST)
                .subscribe {
                    it.findLast { it.type == VitalItem.VitalType.BANNER }?.let {
                        viewState.showVitalBottom(it)
                    }
                    it.filter { it.type == VitalItem.VitalType.CONTENT_ITEM }.let {
                        if (it.isNotEmpty()) {
                            viewState.showVitalItems(it)
                        }
                    }
                }
                .addToDisposable()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    private fun loadReleases(pageNum: Int) {
        currentPage = pageNum
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        releaseInteractor
                .loadReleases(pageNum)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ releaseItems ->
                    viewState.setEndless(!releaseItems.isEnd())
                    showData(releaseItems.data)
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun showData(data: List<ReleaseItem>) {
        if (isFirstPage()) {
            currentItems.clear()
            currentItems.addAll(data)
            viewState.showReleases(data)
        } else {
            currentItems.addAll(data)
            viewState.insertMore(data)
        }
    }

    fun refreshReleases() {
        loadReleases(START_PAGE)
    }

    fun loadMore() {
        loadReleases(currentPage + 1)
    }

    fun onFastSearchOpen(){
        fastSearchAnalytics.open(AnalyticsConstants.screen_releases_list)
    }

    fun onItemClick(item: ReleaseItem) {
        releaseAnalytics.open(AnalyticsConstants.screen_releases_list, item.id)
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code, item))
    }

    fun onCopyClick(item:ReleaseItem){
        releaseAnalytics.copyLink(AnalyticsConstants.screen_releases_list, item.id)
    }

    fun onShareClick(item: ReleaseItem){
        releaseAnalytics.share(AnalyticsConstants.screen_releases_list, item.id)
    }

    fun onShortcutClick(item: ReleaseItem){
        releaseAnalytics.shortcut(AnalyticsConstants.screen_releases_list, item.id)
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}
