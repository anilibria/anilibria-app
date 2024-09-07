package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.interactors.FilterForm
import ru.radiationx.data.interactors.FilterInteractor
import ru.radiationx.data.interactors.FilterType
import ru.radiationx.shared_app.controllers.loaderpage.PageLoader
import ru.radiationx.shared_app.controllers.loaderpage.toDataAction
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader

data class FilterExtra(
    val type: FilterType,
    val genre: String?
)

class FilterViewModel(
    private val argExtra: FilterExtra,
    private val filterInteractor: FilterInteractor
) : ViewModel() {

    private val _loaderArg = MutableStateFlow(LoaderArg())

    private val filterDataLoader = SingleLoader(viewModelScope) {
        filterInteractor.getFilterData(argExtra.type)
    }

    private val releasesLoader = PageLoader<LoaderArg, List<Release>>(viewModelScope) { arg ->
        val result = filterInteractor.getReleases(
            type = argExtra.type,
            page = page,
            form = arg.form,
            collectionType = arg.collectionType
        )
        toDataAction(!result.isEnd()) { it.orEmpty() + result.data }
    }

    private val _collections = MutableStateFlow(argExtra.type.toCollectionsState())
    val collections = _collections.asStateFlow()

    init {
        _loaderArg
            .map { it.collectionType }
            .distinctUntilChanged()
            .map { CollectionsState(selected = it) }
            .launchIn(viewModelScope)

        _loaderArg
            .onEach { releasesLoader.refresh(it) }
            .launchIn(viewModelScope)
        filterDataLoader.refresh()
    }

    fun selectCollection(type: CollectionType?) {
        _loaderArg.update { it.copy(collectionType = type) }
    }

    fun refresh() {
        filterDataLoader.refresh()
        releasesLoader.refresh(_loaderArg.value)
    }

    fun loadMore() {
        releasesLoader.loadMore()
    }

    private data class LoaderArg(
        val form: FilterForm = FilterForm.empty(),
        val collectionType: CollectionType? = null
    )

    private fun FilterType.toCollectionsState(): CollectionsState = when (this) {
        FilterType.Collections -> CollectionsState(
            CollectionType.Planned,
            CollectionType.knownTypes
        )

        else -> CollectionsState()
    }
}

data class CollectionsState(
    val selected: CollectionType? = null,
    val items: Set<CollectionType> = emptySet()
)