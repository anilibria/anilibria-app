package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.interactors.CollectionsInteractor
import ru.radiationx.data.interactors.FilterForm
import ru.radiationx.data.interactors.FilterInteractor
import ru.radiationx.data.interactors.FilterType
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader
import toothpick.InjectConstructor

data class FilterExtra(
    val type: FilterType,
    val genre: String?
)

@InjectConstructor
class FilterViewModel(
    private val argExtra: FilterExtra,
    private val filterInteractor: FilterInteractor,
    private val collectionsInteractor: CollectionsInteractor
) : ViewModel() {

    private val _loaderArg = MutableStateFlow(LoaderArg())

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

    private val _collections = MutableStateFlow(argExtra.type.toCollectionsState())
    val collections = _collections.asStateFlow()

    init {
        initCollections()

        _loaderArg
            .drop(1)
            .onEach { releasesLoader.refresh(it) }
            .launchIn(viewModelScope)
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