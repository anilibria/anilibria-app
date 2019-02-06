package ru.radiationx.anilibria.presentation.favorites

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.repository.FavoriteRepository
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.navigation.AppRouter
import ru.radiationx.anilibria.utils.mvp.BasePresenter

/**
 * Created by radiationx on 13.01.18.
 */
@InjectViewState
class FavoritesPresenter(
        private val favoriteRepository: FavoriteRepository,
        private val router: AppRouter,
        private val errorHandler: IErrorHandler
) : BasePresenter<FavoritesView>(router) {


    companion object {
        private const val START_PAGE = 1
    }

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
        if (!query.isEmpty()) {
            val searchRes = currentReleases.filter {
                it.title.orEmpty().contains(query, true) || it.titleEng.orEmpty().contains(query, true)
            }
            viewState.showReleases(searchRes)
        } else {
            viewState.showReleases(currentReleases)
        }
    }

    fun onItemClick(item: ReleaseItem) {
        router.navigateTo(Screens.ReleaseDetails(item.id, item.code, item))
    }

    fun onItemLongClick(item: ReleaseItem): Boolean {
        return false
    }

}