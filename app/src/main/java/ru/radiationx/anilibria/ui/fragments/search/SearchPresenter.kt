package ru.radiationx.anilibria.ui.fragments.search

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.releases.ReleaseItem
import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter

@InjectViewState
class SearchPresenter : BasePresenter<SearchView>() {

    private val START_PAGE = 1
    private var currentPage = START_PAGE
    var currentGenre: String? = null
    var currentQuery: String? = null


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("SUKA", "onFirstViewAttach")
        loadGenres()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }


    private fun loadGenres() {
        val disposable = Api.get().Releases().getGenres()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ genres ->
                    Log.d("SUKA", "subscribe call show")
                    viewState.setRefreshing(false)
                    viewState.showGenres(genres)
                }) { throwable ->
                    viewState.setRefreshing(false)
                    Log.d("SUKA", "SAS")
                    throwable.printStackTrace()
                }
        addDisposable(disposable)
    }

    private fun loadReleases(pageNum: Int) {
        Log.e("SUKA", "loadReleases")
        currentPage = pageNum
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        val disposable = Api.get().Releases().search(currentQuery.orEmpty(), currentGenre.orEmpty(), pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ releaseItems ->
                    Log.d("SUKA", "subscribe call show")
                    if (isFirstPage()) {
                        viewState.setRefreshing(false)
                        viewState.showReleases(releaseItems)
                    } else {
                        viewState.insertMore(releaseItems)
                    }
                }) { throwable ->
                    viewState.setRefreshing(false)
                    Log.d("SUKA", "SAS")
                    throwable.printStackTrace()
                }
        addDisposable(disposable)
    }

    internal fun refreshReleases() {
        loadReleases(START_PAGE)
    }

    internal fun loadMore() {
        loadReleases(currentPage + 1)
    }

    internal fun onItemClick(item: ReleaseItem) {
        val args = Bundle()
        args.putInt(ReleaseFragment.ARG_ID, item.id)
        args.putSerializable(ReleaseFragment.ARG_ITEM, item)
        App.get().router.navigateTo(Screens.RELEASE_DETAILS, args)
    }

    internal fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }
}
