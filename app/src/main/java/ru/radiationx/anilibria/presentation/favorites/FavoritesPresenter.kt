package ru.radiationx.anilibria.presentation.favorites

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.entity.app.release.FavoriteData
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 13.01.18.
 */
@InjectViewState
class FavoritesPresenter(
        private val releaseRepository: ReleaseRepository,
        private val router: Router
) : BasePresenter<FavoritesView>(router) {


    companion object {
        private const val START_PAGE = 1
    }

    private var currentPage = START_PAGE

    private var currentSessId = ""

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
        releaseRepository.getFavorites2()
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onLoad(it)
                }) { throwable ->
                    throwable.printStackTrace()
                }
                .addToDisposable()
    }

    private fun onLoad(favData: FavoriteData) {
        currentSessId = favData.sessId
        viewState.setEndless(!favData.items.isEnd())
        if (isFirstPage()) {
            viewState.showReleases(favData.items.data)
        } else {
            viewState.insertMore(favData.items.data)
        }
    }

    fun refreshReleases() {
        loadReleases(START_PAGE)
    }

    fun loadMore() {
        loadReleases(currentPage + 1)
    }

    fun deleteFav(id: Int) {
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        releaseRepository.deleteFavorite(id, currentSessId)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onLoad(it)
                }) { throwable ->
                    throwable.printStackTrace()
                }
                .addToDisposable()
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