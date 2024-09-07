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
import ru.radiationx.data.apinext.models.Genre
import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.apinext.models.filters.FormItem
import ru.radiationx.data.datasource.holders.ReleaseUpdateHolder
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.data.interactors.CollectionsInteractor
import ru.radiationx.data.interactors.FilterForm
import ru.radiationx.data.interactors.FilterInteractor
import ru.radiationx.data.interactors.FilterType
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.mapData
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader
import toothpick.InjectConstructor

data class FilterExtra(
    val type: FilterType,
    val genre: Genre?
) : QuillExtra

@InjectConstructor
class FilterViewModel(
    private val argExtra: FilterExtra,
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

    private val filterDataLoader = SingleLoader(viewModelScope) {
        filterInteractor.getFilterData(argExtra.type)
    }

    private val collectionsLoader = SingleLoader(viewModelScope) {
        collectionsInteractor.loadReleaseIds()
    }

    private val releasesLoader = PageLoader<LoaderArg, List<Release>>(viewModelScope) { arg ->
        val result = filterInteractor.getReleases(
            filterType = argExtra.type,
            page = page,
            form = arg.form,
            collectionType = arg.collectionType
        )
        toDataAction(!result.isEnd()) { it.orEmpty() + result.data }
    }

    private val _state = MutableStateFlow(SearchScreenState())
    val state = _state.asStateFlow()

    init {
        argExtra.genre?.also { genre ->
            updateForm { it.copy(genres = it.genres.plus(FormItem.Genre(genre.id))) }
        }
        initCollections()
        initScreenState()

        _loaderArg
            .drop(1)
            .onEach { releasesLoader.refresh(it) }
            .launchIn(viewModelScope)

        _queryState
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { query ->
                updateForm { it.copy(query = query) }
            }
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
                    it.copy(data = loadingState)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateForm(block: (FilterForm) -> FilterForm) {
        _loaderArg.update { it.copy(form = block(it.form)) }
    }

    fun selectCollection(type: CollectionType?) {
        _loaderArg.update { it.copy(collectionType = type) }
    }

    fun refresh() {
        if (argExtra.type == FilterType.Collections) {
            collectionsLoader.refresh()
        }
        filterDataLoader.refresh()
        releasesLoader.refresh(_loaderArg.value)
    }

    fun loadMore() {
        releasesLoader.loadMore()
    }

    fun onQueryChange(query: String) {
        _queryState.value = query
    }

    fun onItemClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        catalogAnalytics.releaseClick()
        releaseAnalytics.open(AnalyticsConstants.screen_catalog, releaseItem.id.id)
        router.navigateTo(Screens.ReleaseDetails(releaseItem.id, releaseItem.code, releaseItem))
    }

    fun onCopyClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.copyToClipBoard(releaseItem.link.orEmpty())
        releaseAnalytics.copyLink(AnalyticsConstants.screen_catalog, releaseItem.id.id)
    }

    fun onShareClick(item: ReleaseItemState) {
        val releaseItem = findRelease(item.id) ?: return
        systemUtils.shareText(releaseItem.link.orEmpty())
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