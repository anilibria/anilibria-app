package ru.radiationx.anilibria.ui.fragments.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.loading.DataLoadingController
import ru.radiationx.anilibria.model.loading.PageLoadParams
import ru.radiationx.anilibria.model.loading.ScreenStateAction
import ru.radiationx.anilibria.model.loading.mapData
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.FavoritesAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.repository.FavoriteRepository
import ru.radiationx.shared.ktx.coRunCatching
import ru.radiationx.shared_app.common.SystemUtils
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val router: Router,
    private val errorHandler: IErrorHandler,
    private val favoritesAnalytics: FavoritesAnalytics,
    private val releaseAnalytics: ReleaseAnalytics,
    releaseUpdateHolder: ReleaseUpdateHolder,
    private val shortcutHelper: ShortcutHelper,
    private val systemUtils: SystemUtils,
) : ViewModel() {

    private val loadingController = DataLoadingController(viewModelScope) {
        submitPageAnalytics(it.page)
        getDataSource(it)
    }

    private val _state = MutableStateFlow(FavoritesScreenState())
    val state = _state.asStateFlow()

    private var lastLoadedPage: Int? = null
    private val queryState = MutableStateFlow("")

    init {

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
                _state.update {
                    it.copy(searchItems = searchItems)
                }
            }
            .launchIn(viewModelScope)

        combine(
            loadingController.observeState(),
            updatesMapFlow,
        ) { loadingState, updates ->
            loadingState.mapData { items ->
                items.map { it.toState(updates) }
            }
        }
            .onEach { loadingState ->
                _state.update {
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)

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
        viewModelScope.launch {
            _state.update {
                it.copy(deletingItemIds = it.deletingItemIds + id)
            }
            coRunCatching {
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
            _state.update {
                it.copy(deletingItemIds = it.deletingItemIds - id)
            }
        }
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_favorites, releaseItem.id.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.shareText(releaseItem.link.orEmpty())
        releaseAnalytics.share(AnalyticsConstants.screen_favorites, releaseItem.id.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        shortcutHelper.addShortcut(releaseItem)
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