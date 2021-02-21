package ru.radiationx.anilibria.presentation.favorites

import moxy.InjectViewState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.FavoritesAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.repository.FavoriteRepository
import ru.terrakok.cicerone.Router
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
@InjectViewState
class FavoritesPresenter @Inject constructor(
        private val favoriteRepository: FavoriteRepository,
        private val router: Router,
        private val errorHandler: IErrorHandler,
        private val favoritesAnalytics: FavoritesAnalytics,
        private val releaseAnalytics: ReleaseAnalytics
) : BasePresenter<FavoritesView>(router) {


    companion object {
        private const val START_PAGE = 1
    }

    private var lastLoadedPage:Int?=null
    private var isSearchEnabled:Boolean = false

    private var currentPage = START_PAGE

    private val currentReleases = mutableListOf<ReleaseItem>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refreshReleases()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    private fun loadReleases(pageNum: Int) {
        if(lastLoadedPage!=pageNum){
            favoritesAnalytics.loadPage(pageNum)
            lastLoadedPage = pageNum
        }
        currentPage = pageNum
        if (isFirstPage()) {
            viewState.setRefreshing(true)
        }
        favoriteRepository
                .getFavorites(pageNum)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.setEndless(!it.isEnd())
                    currentReleases.addAll(it.data)
                    showData(it.data)
                }) {
                    showData(emptyList())
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    private fun showData(data: List<ReleaseItem>) {
        if (isFirstPage()) {
            viewState.showReleases(data)
        } else {
            viewState.insertMore(data)
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
        favoriteRepository
                .deleteFavorite(id)
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.removeReleases(listOf(it))
                }) {
                    errorHandler.handle(it)
                }
                .addToDisposable()
    }

    fun localSearch(query: String) {
        isSearchEnabled = query.isNotEmpty()
        if (query.isNotEmpty()) {
            val searchRes = currentReleases.filter {
                it.title.orEmpty().contains(query, true) || it.titleEng.orEmpty().contains(query, true)
            }
            viewState.showReleases(searchRes)
        } else {
            viewState.showReleases(currentReleases)
        }
    }

    fun onSearchClick(){
        favoritesAnalytics.searchClick()
    }

    fun onItemClick(item: ReleaseItem) {
        if(isSearchEnabled){
            favoritesAnalytics.searchReleaseClick()
        }else{
            favoritesAnalytics.releaseClick()
        }
        releaseAnalytics.open(AnalyticsConstants.screen_favorites, item.id)
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code, item))
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }

}