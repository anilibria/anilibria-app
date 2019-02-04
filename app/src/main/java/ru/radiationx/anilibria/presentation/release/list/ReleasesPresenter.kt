package ru.radiationx.anilibria.presentation.release.list

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.holders.ReleaseUpdateHolder
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.presentation.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.radiationx.anilibria.ui.navigation.AppRouter

/* Created by radiationx on 05.11.17. */

@InjectViewState
class ReleasesPresenter(
        private val releaseInteractor: ReleaseInteractor,
        private val vitalRepository: VitalRepository,
        private val router: AppRouter,
        private val errorHandler: IErrorHandler,
        private val releaseUpdateHolder: ReleaseUpdateHolder
) : BasePresenter<ReleasesView>(router) {

    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE
    private val currentItems = mutableListOf<ReleaseItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("S_DEF_LOG", "onFirstViewAttach")
        refreshReleases()
        loadVital()

        releaseUpdateHolder
                .observeEpisodes()
                .subscribe { data ->
                    val itemsNeedUpdate = mutableListOf<ReleaseItem>()
                    currentItems.forEach { item ->
                        data.firstOrNull { it.id == item.id }?.also { updItem ->
                            val isNew = item.torrentUpdate > updItem.lastOpenTimestamp || item.torrentUpdate > updItem.timestamp
                            Log.e("lalalupdata", "check pres ${item.id}, ${item.torrentUpdate} : ${updItem.id}, ${updItem.timestamp}, ${updItem.lastOpenTimestamp} : ${item.isNew}, $isNew")
                            if (item.isNew != isNew) {
                                item.isNew = isNew
                                itemsNeedUpdate.add(item)
                            }
                        }
                    }
                    Log.e("lalalupdata", "pres updateReleases: ${itemsNeedUpdate.joinToString { it.id.toString() }}")

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
        Log.e("S_DEF_LOG", "loadReleases")
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

    fun onItemClick(item: ReleaseItem) {
        val args = Bundle()
        args.putInt(ReleaseFragment.ARG_ID, item.id)
        args.putString(ReleaseFragment.ARG_ID_CODE, item.code)
        args.putSerializable(ReleaseFragment.ARG_ITEM, item)
        router.navigateTo(Screens.ReleaseDetails(args))
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }

    fun openSearch() {
        router.navigateTo(Screens.ReleasesSearch())
    }
}
