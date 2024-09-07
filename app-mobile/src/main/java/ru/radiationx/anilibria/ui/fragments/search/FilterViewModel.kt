package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
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


    private val _collectionType = MutableStateFlow<CollectionType?>(null)
    private val _form = MutableStateFlow(FilterForm.empty())

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
        toDataAction(!result.isEnd()) {
            it.orEmpty() + result.data
        }
    }

    fun selectCollection(type: CollectionType?) {

    }

    fun refresh() {

    }

    fun loadMore() {

    }

    private data class LoaderArg(
        val form: FilterForm,
        val collectionType: CollectionType?
    )
}