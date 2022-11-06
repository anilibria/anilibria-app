package ru.radiationx.anilibria.presentation.favorites

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.*
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.BasePresenter
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.ui.fragments.favorites.FavoritesScreenState
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.FavoritesAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
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
    private val releaseAnalytics: ReleaseAnalytics,
    private val releaseUpdateHolder: ReleaseUpdateHolder
) : BasePresenter<FavoritesView>(router) {

    private val loadingController = DataLoadingController(presenterScope) {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }

    private val stateController = StateController(FavoritesScreenState())

    private var lastLoadedPage: Int? = null
    private val queryState = MutableStateFlow("")

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        stateController
            .observeState()
            .onEach { viewState.showState(it) }
            .launchIn(presenterScope)

        val updatesMapFlow = releaseUpdateHolder.observeEpisodes().map { updates ->
            updates.associateBy { it.id }
        }

        combine(
            loadingController.observeState().mapNotNull { it.data }.distinctUntilChanged(),
            updatesMapFlow,
            queryState
        ) { currentItems, updates, query ->
            currentItems.filterByQuery(query).map { it.toState(updates) }
        }
            .onEach { searchItems ->
                stateController.updateState {
                    it.copy(searchItems = searchItems)
                }
            }
            .launchIn(presenterScope)

        combine(
            loadingController.observeState(),
            updatesMapFlow,
        ) { loadingState, updates ->
            loadingState.mapData { items ->
                items.map { it.toState(updates) }
            }
        }
            .onEach { loadingState ->
                stateController.updateState {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(presenterScope)

        refreshReleases()
    }

    fun refreshReleases() {
        loadingController.refresh()
    }

    fun loadMore() {
        loadingController.loadMore()
    }

    fun deleteFav(id: ReleaseId) {
        favoritesAnalytics.deleteFav()
        presenterScope.launch {
            stateController.updateState {
                it.copy(deletingItemIds = it.deletingItemIds + id)
            }
            runCatching {
                favoriteRepository.deleteFavorite(id)
            }.onSuccess { deletedItem ->
                loadingController.currentState.data?.also { dataState ->
                    val newItems = dataState.toMutableList()
                    newItems.find { it.id == deletedItem.id }?.also {
                        newItems.remove(it)
                    }
                    loadingController.modifyData(newItems)
                }
            }.onFailure {
                errorHandler.handle(it)
            }
            stateController.updateState {
                it.copy(deletingItemIds = it.deletingItemIds - id)
            }
        }
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_favorites, releaseItem.id.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        Utils.shareText(releaseItem.link.orEmpty())
        releaseAnalytics.share(AnalyticsConstants.screen_favorites, releaseItem.id.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        ShortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_favorites, releaseItem.id.id)
    }

    fun localSearch(query: String) {
        queryState.value = query
    }

    fun onSearchClick() {
        favoritesAnalytics.searchClick()
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        if (queryState.value.isNotEmpty()) {
            favoritesAnalytics.searchReleaseClick()
        } else {
            favoritesAnalytics.releaseClick()
        }
        releaseAnalytics.open(AnalyticsConstants.screen_favorites, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    private fun findRelease(id: ReleaseId): Release? {
        return loadingController.currentState.data?.find { it.id == id }
    }

    private fun submitPageAnalytics(page: Int) {
        if (lastLoadedPage != page) {
            favoritesAnalytics.loadPage(page)
            lastLoadedPage = page
        }
    }

    private suspend fun getDataSource(params: PageLoadParams): ScreenStateAction.Data<List<Release>> {
        return try {
            favoriteRepository
                .getFavorites(params.page)
                .let { paginated ->
                    val newItems = if (params.isFirstPage) {
                        paginated.data
                    } else {
                        loadingController.currentState.data.orEmpty() + paginated.data
                    }
                    ScreenStateAction.Data(newItems, !paginated.isEnd())
                }
        } catch (ex: Throwable) {
            if (params.isFirstPage) {
                errorHandler.handle(ex)
            }
            throw ex
        }
    }

    private fun List<Release>.filterByQuery(query: String): List<Release> {
        if (query.isEmpty()) {
            return emptyList()
        }
        return filter {
            it.title.orEmpty().contains(query, true)
                    || it.titleEng.orEmpty().contains(query, true)
        }
    }

}