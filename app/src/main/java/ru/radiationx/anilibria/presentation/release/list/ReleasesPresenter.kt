package ru.radiationx.anilibria.presentation.release.list;

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/* Created by radiationx on 05.11.17. */

@InjectViewState
class ReleasesPresenter(
        private val releaseRepository: ReleaseRepository,
        private val vitalRepository: VitalRepository,
        private val router: Router
) : BasePresenter<ReleasesView>(router) {
    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("SUKA", "onFirstViewAttach")
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
                        Log.e("SUKA", "VITAL SET LIST ITEMS ${it.size}")
                        if (it.isNotEmpty()) {
                            viewState.showVitalItems(it)
                        }
                    }
                    router.showSystemMessage("Show vital in REL_LIST: ${it.size}")
                }
                .addToDisposable()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    private fun loadReleases(pageNum: Int) {
        Log.e("SUKA", "loadReleases")
        currentPage = pageNum
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        releaseRepository.getReleases(pageNum)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ releaseItems ->
                    viewState.setEndless(!releaseItems.isEnd())
                    if (isFirstPage()) {
                        viewState.showReleases(releaseItems.data)
                    } else {
                        viewState.insertMore(releaseItems.data)
                    }
                }) { throwable ->
                    throwable.printStackTrace()
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
        args.putString(ReleaseFragment.ARG_ID_NAME, item.idName)
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
