package ru.radiationx.anilibria.presentation.favorites

import moxy.InjectViewState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.release.list.ReleaseScreenState
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
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

    private var lastLoadedPage: Int? = null
    private var isSearchEnabled: Boolean = false
    private var currentQuery: String = ""

    private var currentPage = START_PAGE

    private val currentReleases = mutableListOf<ReleaseItem>()

    private var currentState = ReleaseScreenState()

    private fun updateState(block: (ReleaseScreenState) -> ReleaseScreenState) {
        currentState = block.invoke(currentState)
        viewState.showState(currentState)
    }

    private fun findRelease(id: Int): ReleaseItem? {
        return currentReleases.find { it.id == id }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refreshReleases()
    }

    private fun isFirstPage(): Boolean {
        return currentPage == START_PAGE
    }

    private fun loadReleases(pageNum: Int) {
        if (lastLoadedPage != pageNum) {
            favoritesAnalytics.loadPage(pageNum)
            lastLoadedPage = pageNum
        }
        currentPage = pageNum
        if (isFirstPage()) {
            updateState { it.copy(refreshing = true) }
        }
        favoriteRepository
            .getFavorites(pageNum)
            .doAfterTerminate {
                updateState { it.copy(refreshing = false) }
            }
            .subscribe({
                if (isFirstPage()) {
                    currentReleases.clear()
                }
                currentReleases.addAll(it.data)
                updateData()
            }) {
                errorHandler.handle(it)
            }
            .addToDisposable()
    }

    private fun updateData() {
        isSearchEnabled = currentQuery.isNotEmpty()
        val newReleases = if (currentQuery.isNotEmpty()) {
            val searchRes = currentReleases.filter {
                it.title.orEmpty().contains(currentQuery, true) || it.titleEng.orEmpty()
                    .contains(currentQuery, true)
            }
            searchRes
        } else {
            currentReleases
        }
        val newItems = newReleases.map { it.toState() }
        updateState { it.copy(items = newItems) }
    }

    fun refreshReleases() {
        loadReleases(START_PAGE)
    }

    fun loadMore() {
        loadReleases(currentPage + 1)
    }

    fun deleteFav(id: Int) {
        favoritesAnalytics.deleteFav()
        updateState { it.copy(refreshing = true) }
        favoriteRepository
            .deleteFavorite(id)
            .doAfterTerminate {
                updateState { it.copy(refreshing = false) }
            }
            .subscribe({ deletedItem ->
                updateState {
                    val newItems = it.items.toMutableList()
                    newItems.find { it.id == deletedItem.id }?.also {
                        newItems.remove(it)
                    }
                    it.copy(items = newItems)
                }
            }) {
                errorHandler.handle(it)
            }
            .addToDisposable()
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_favorites, releaseItem.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.shareText(releaseItem.link.orEmpty())
        releaseAnalytics.share(AnalyticsConstants.screen_favorites, releaseItem.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        ShortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_favorites, releaseItem.id)
    }

    fun localSearch(query: String) {
        currentQuery = query
        updateData()
    }

    fun onSearchClick() {
        favoritesAnalytics.searchClick()
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        if (isSearchEnabled) {
            favoritesAnalytics.searchReleaseClick()
        } else {
            favoritesAnalytics.releaseClick()
        }
        releaseAnalytics.open(AnalyticsConstants.screen_favorites, releaseItem.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }


}