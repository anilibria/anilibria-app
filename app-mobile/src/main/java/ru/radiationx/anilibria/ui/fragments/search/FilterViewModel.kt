package ru.radiationx.anilibria.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.radiationx.data.apinext.models.enums.CollectionType
import ru.radiationx.data.interactors.FilterInteractor
import ru.radiationx.data.interactors.FilterType
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader

data class FilterExtra(
    val type: FilterType,
    val genre: String?
)

class FilterViewModel(
    private val argExtra: FilterExtra,
    private val filterInteractor: FilterInteractor
) : ViewModel() {


    private val filterDataLoader = SingleLoader(viewModelScope) {
        filterInteractor.getFilterData(argExtra.type)
    }

    fun selectCollection(type: CollectionType?) {

    }

    fun refresh() {

    }

    fun loadMore() {

    }
}