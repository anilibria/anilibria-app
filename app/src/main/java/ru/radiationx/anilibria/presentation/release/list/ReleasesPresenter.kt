package ru.radiationx.anilibria.presentation.release.list

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.presentation.ErrorHandler
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/* Created by radiationx on 05.11.17. */

@InjectViewState
class ReleasesPresenter(
        private val releaseRepository: ReleaseRepository,
        private val vitalRepository: VitalRepository,
        private val router: Router,
        private val errorHandler: ErrorHandler
) : BasePresenter<ReleasesView>(router) {
    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("S_DEF_LOG", "onFirstViewAttach")
        refreshReleases()
        loadVital()
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
        releaseRepository
                .getReleases(pageNum)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ releaseItems ->
                    viewState.setEndless(!releaseItems.isEnd())
                    if (isFirstPage()) {
                        viewState.showReleases(releaseItems.data)
                    } else {
                        viewState.insertMore(releaseItems.data)
                    }
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
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
        args.putString(ReleaseFragment.ARG_ID_CODE, item.idName)
        args.putSerializable(ReleaseFragment.ARG_ITEM, item)
        router.navigateTo(Screens.RELEASE_DETAILS, args)
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }

    fun openSearch() {
        router.navigateTo(Screens.RELEASES_SEARCH)
    }
}
