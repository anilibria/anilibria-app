package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.anilibria.model.ReleaseItemState
import ru.radiationx.anilibria.model.toState
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.features.CatalogAnalytics
import ru.radiationx.data.analytics.features.ReleaseAnalytics
import ru.radiationx.data.api.collections.CollectionsInteractor
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.shared.filter.FilterForm
import ru.radiationx.data.api.shared.filter.FilterInteractor
import ru.radiationx.data.api.shared.filter.FilterType
import ru.radiationx.data.app.releaseupdate.ReleaseUpdateHolder
import ru.radiationx.data.common.ReleaseId
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.mapData
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader
import toothpick.InjectConstructor

data class SearchExtra(
    val type: FilterType,
) : QuillExtra

@InjectConstructor
class SearchViewModel(
    private val argExtra: SearchExtra,
    private val filterInteractor: FilterInteractor,
    private val collectionsInteractor: CollectionsInteractor,
    private val releaseUpdateHolder: ReleaseUpdateHolder,
    private val systemUtils: SystemUtils,
    private val releaseAnalytics: ReleaseAnalytics,
    private val catalogAnalytics: CatalogAnalytics,
    private val shortcutHelper: ShortcutHelper,
    private val router: Router
) : ViewModel() {

    private val _collections = MutableStateFlow(argExtra.type.toCollectionsState())
    val collections = _collections.asStateFlow()

    private val _loaderArg =
        MutableStateFlow(LoaderArg(collectionType = collections.value?.selected))

    private val _queryState = MutableStateFlow("")

    private val collectionsLoader = SingleLoader(viewModelScope) {
        collectionsInteractor.loadReleaseIds()
    }

    private val releasesLoader = PageLoader<List<Release>>(viewModelScope) {
        val result = filterInteractor.getReleases(
            filterType = argExtra.type,
            page = it.page,
            query = _queryState.value,
            form = _loaderArg.value.form,
            collectionType = _loaderArg.value.collectionType
        )
        it.toDataAction(!result.isEnd()) { it.orEmpty() + result.data }
    }

    private val _state = MutableStateFlow(SearchScreenState())
    val state = _state.asStateFlow()

    init {
        initCollections()
        initScreenState()

        _loaderArg
            .drop(1)
            .debounce(300)
            .onEach { refresh() }
            .launchIn(viewModelScope)

        _queryState
            .drop(1)
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { refresh() }
            .launchIn(viewModelScope)

        refresh()
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
        if (argExtra.type == FilterType.Collections && collectionsLoader.isNeedRefresh()) {
            collectionsLoader.refresh()
        }
        releasesLoader.refresh()
    }

    fun loadMore() {
        releasesLoader.loadMore()
    }

    fun onQueryChange(query: String) {
        _queryState.value = query
    }

    fun onFormChanged(newForm: FilterForm) {
        _loaderArg.update { it.copy(form = newForm) }
    }

    fun onCollectionChanged(type: CollectionType?) {
        _loaderArg.update { it.copy(collectionType = type) }
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        catalogAnalytics.releaseClick()
        releaseAnalytics.open(AnalyticsConstants.screen_catalog, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem))
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.copy(releaseItem.link)
        releaseAnalytics.copyLink(AnalyticsConstants.screen_catalog, releaseItem.id.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.share(releaseItem.link)
        releaseAnalytics.share(AnalyticsConstants.screen_catalog, releaseItem.id.id)
    }

    fun onShortcutClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        shortcutHelper.addShortcut(releaseItem)
        releaseAnalytics.shortcut(AnalyticsConstants.screen_catalog, releaseItem.id.id)
    }

    private fun findRelease(id: ReleaseId): Release? {
        return releasesLoader.getData()?.find { it.id == id }
    }

    private fun initCollections() {
        if (argExtra.type != FilterType.Collections) {
            return
        }
        _loaderArg
            .mapNotNull { it.collectionType }
            .distinctUntilChanged()
            .map { type ->
                _collections.update {
                    it?.copy(selected = type)
                }
            }
            .launchIn(viewModelScope)

        collectionsInteractor
            .observeIdsGrouped()
            .map { it.keys.filterIsInstance<CollectionType.Unknown>() }
            .distinctUntilChanged()
            .onEach { unknownTypes ->
                _collections.update {
                    it?.copy(types = CollectionType.knownTypes + unknownTypes)
                }
            }
            .launchIn(viewModelScope)
    }

    private data class LoaderArg(
        val form: FilterForm = FilterForm.empty(),
        val collectionType: CollectionType? = null
    )

    private fun FilterType.toCollectionsState(): CollectionsState? = when (this) {
        FilterType.Collections -> CollectionsState()

        else -> null
    }
}

data class CollectionsState(
    val selected: CollectionType = CollectionType.Planned,
    val types: Set<CollectionType> = CollectionType.knownTypes
)