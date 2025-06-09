package ru.radiationx.anilibria.ui.fragments.search.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.ui.fragments.search.controller.SearchController
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.api.collections.CollectionsInteractor
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.favorites.FavoritesInteractor
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.filter.FilterInteractor
import ru.radiationx.data.api.shared.filter.FilterType
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateHolder
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.mapData
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import javax.inject.Inject

data class SearchTabExtra(
    val type: FilterType,
    val collectionType: CollectionType?
) : QuillExtra

class SearchTabViewModel @Inject constructor(
    private val argExtra: SearchTabExtra,
    private val searchController: SearchController,
    private val filterInteractor: FilterInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val collectionsInteractor: CollectionsInteractor,
    private val releaseUpdateHolder: ReleaseUpdateHolder,
    private val systemUtils: SystemUtils,
    private val releaseAnalytics: ReleaseAnalytics,
    private val catalogAnalytics: CatalogAnalytics,
    private val shortcutHelper: ShortcutHelper,
    private val router: Router
) : ViewModel() {

    private val releasesLoader = PageLoader<List<Release>>(viewModelScope) {
        val loaderArg = searchController.loaderArg.value
        val result = filterInteractor.getReleases(
            filterType = argExtra.type,
            page = it.page,
            query = loaderArg.query,
            form = loaderArg.form,
            collectionType = argExtra.collectionType
        )
        it.toDataAction(!result.isEnd()) { it.orEmpty() + result.data }
    }

    private val _state = MutableStateFlow(SearchTabState())
    val state = _state.asStateFlow()

    private val _contextEvent = MutableSharedFlow<Release>()
    val contextEvent = _contextEvent.asSharedFlow()

    init {
        initScreenState()

        searchController
            .loaderArg
            .filter { it.collectionType == argExtra.collectionType }
            .distinctUntilChanged()
            .onEach { refresh() }
            .launchIn(viewModelScope)

        if (argExtra.type == FilterType.Favorites) {
            favoritesInteractor
                .observeIds()
                .drop(1)
                .distinctUntilChanged()
                .onEach { refresh() }
                .launchIn(viewModelScope)
        }

        if (argExtra.type == FilterType.Collections) {
            collectionsInteractor
                .observeIds()
                .drop(1)
                .distinctUntilChanged()
                .onEach { refresh() }
                .launchIn(viewModelScope)
        }
    }

    private fun initScreenState() {
        combine(
            releasesLoader.observeState(),
            releaseUpdateHolder.observeEpisodes()
        ) { loadingState, updates ->
            val updatesMap = updates.associateBy { it.id }
            loadingState.mapData { items ->
                items.map { it.toState(updatesMap) }
            }
        }
            .onEach { loadingState ->
                _state.update {
                    it.copy(releases = loadingState)
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        releasesLoader.refresh()
    }

    fun loadMore() {
        releasesLoader.loadMore()
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        catalogAnalytics.releaseClick()
        releaseAnalytics.open(AnalyticsConstants.screen_catalog, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem))
    }

    fun onItemContextClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        viewModelScope.launch {
            _contextEvent.emit(releaseItem)
        }
    }

    private fun findRelease(id: ReleaseId): Release? {
        return releasesLoader.getData()?.find { it.id == id }
    }

}