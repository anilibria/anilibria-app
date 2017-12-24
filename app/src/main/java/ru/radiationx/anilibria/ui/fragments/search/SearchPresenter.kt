package ru.radiationx.anilibria.ui.fragments.search

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.data.api.models.ReleaseItem
import ru.radiationx.anilibria.data.repository.ReleasesRepository
import ru.radiationx.anilibria.ui.fragments.release.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

@InjectViewState
class SearchPresenter(private val releasesRepository: ReleasesRepository,
                      private val router: Router) : BasePresenter<SearchView>(router) {

    private val START_PAGE = 1
    private var currentPage = START_PAGE
    var currentGenre: String? = null
    var currentQuery: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.e("SUKA", "onFirstViewAttach")
        loadGenres()
        loadReleases(START_PAGE)
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    fun fastSearch(query: String) {
        val disposable = releasesRepository.fastSearch(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ searchItems ->
                    Log.d("SUKA", "subscribe call show")
                    viewState.setRefreshing(false)
                    viewState.showFastItems(searchItems)
                }) { throwable ->
                    viewState.setRefreshing(false)
                    Log.d("SUKA", "SAS")
                    throwable.printStackTrace()
                }
        addDisposable(disposable)
    }

    private fun loadGenres() {
        val disposable = releasesRepository.getGenres()
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

    fun isEmpty(): Boolean = currentQuery.isNullOrEmpty() && currentGenre.isNullOrEmpty()

    private fun loadReleases(pageNum: Int) {
        Log.e("SUKA", "loadReleases")

        if (isEmpty()) {
            viewState.setRefreshing(false)
            return
        }

        currentPage = pageNum
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        val disposable = releasesRepository.searchRelease(currentQuery.orEmpty(), currentGenre.orEmpty(), pageNum)
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
}
