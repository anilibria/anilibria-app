package ru.radiationx.anilibria.ui.fragments.releases;

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.releases.ReleaseItem
import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/* Created by radiationx on 05.11.17. */

@InjectViewState
class ReleasesPresenter(private val router: Router) : BasePresenter<ReleasesView>(router) {
    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("SUKA", "onFirstViewAttach")
        refreshReleases()
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
        val disposable = Api.get().Releases().getItems(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ releaseItems ->
                    Log.d("SUKA", "subscribe call show")
                    viewState.setEndless(!releaseItems.isEnd())
                    if (isFirstPage()) {
                        viewState.setRefreshing(false)
                        viewState.showReleases(releaseItems.data)
                    } else {
                        viewState.insertMore(releaseItems.data)
                    }
                }) { throwable ->
                    viewState.setRefreshing(false)
                    Log.d("SUKA", "SAS")
                    throwable.printStackTrace()
                }
        addDisposable(disposable)
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
