package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.anilibria.ui.fragments.search.controller.SearchController
import ru.radiationx.data.api.collections.CollectionsInteractor
import ru.radiationx.data.api.collections.models.CollectionType
import ru.radiationx.data.api.shared.filter.FilterForm
import ru.radiationx.data.api.shared.filter.FilterType
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader
import javax.inject.Inject

data class SearchExtra(
    val type: FilterType,
) : QuillExtra

class SearchViewModel @Inject constructor(
    private val argExtra: SearchExtra,
    private val searchController: SearchController,
    private val collectionsInteractor: CollectionsInteractor,
) : ViewModel() {

    private val _collections = MutableStateFlow(argExtra.type.toCollectionsState())
    val collections = _collections.asStateFlow()

    private val _queryState = MutableStateFlow("")

    private val collectionsLoader = SingleLoader(viewModelScope) {
        collectionsInteractor.loadReleaseIds()
    }

    init {
        initCollections()
        _queryState
            .drop(1)
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { query ->
                searchController.loaderArg.update {
                    it.copy(query = query)
                }
            }
            .launchIn(viewModelScope)

        _collections
            .onEach { state ->
                searchController.loaderArg.update {
                    it.copy(collectionType = state?.selected)
                }
            }
            .launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        if (argExtra.type == FilterType.Collections) {
            collectionsLoader.refresh()
        }
    }

    fun onQueryChange(query: String) {
        _queryState.value = query
    }

    fun onCollectionChanged(type: CollectionType?) {
        _collections.update {
            it?.copy(selected = type ?: it.selected)
        }
    }

    private fun initCollections() {
        if (argExtra.type != FilterType.Collections) {
            return
        }

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

        collectionsInteractor
            .observeIdsGrouped()
            .map { it.mapValues { it.value.size } }
            .onEach { countsMap ->
                _collections.update {
                    it?.copy(counts = countsMap)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun FilterType.toCollectionsState(): CollectionsState? = when (this) {
        FilterType.Collections -> CollectionsState()

        else -> null
    }
}

data class CollectionsState(
    val selected: CollectionType = CollectionType.Planned,
    val types: Set<CollectionType> = CollectionType.knownTypes,
    val counts: Map<CollectionType, Int> = emptyMap()
)