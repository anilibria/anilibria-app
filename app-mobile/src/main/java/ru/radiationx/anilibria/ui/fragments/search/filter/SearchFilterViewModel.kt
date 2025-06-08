package ru.radiationx.anilibria.ui.fragments.search.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.data.api.releases.models.ReleaseGenre
import ru.radiationx.data.api.shared.filter.FilterForm
import ru.radiationx.data.api.shared.filter.FilterInteractor
import ru.radiationx.data.api.shared.filter.FilterType
import ru.radiationx.data.api.shared.filter.FormItem
import ru.radiationx.quill.QuillExtra
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared_app.controllers.loadersingle.SingleLoader
import javax.inject.Inject

data class SearchFilterExtra(
    val type: FilterType,
    val genre: ReleaseGenre?
) : QuillExtra

class SearchFilterViewModel @Inject constructor(
    private val argExtra: SearchFilterExtra,
    private val filterInteractor: FilterInteractor,
) : ViewModel() {

    private val filterDataLoader = SingleLoader(viewModelScope) {
        filterInteractor.getFilterData(argExtra.type)
    }

    private val _state = MutableStateFlow(SearchFilterState())
    val state = _state.asStateFlow()

    private val _applyEvent = EventFlow<FilterForm>()
    val applyEvent = _applyEvent.observe()

    init {
        argExtra.genre?.also { genre ->
            onGenre(FormItem.Genre(genre.id))
        }
        onApply()
        filterDataLoader
            .observeState()
            .onEach { filterState ->
                _state.update {
                    it.copy(filter = filterState)
                }
            }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        if (filterDataLoader.isNeedRefresh() || argExtra.type != FilterType.Catalog) {
            filterDataLoader.refresh()
        }
    }

    fun onApply() {
        viewModelScope.launch {
            _applyEvent.emit(state.value.form)
        }
    }

    fun onReset() {
        updateForm {
            FilterForm.empty()
        }
    }

    fun onResetGenres() {
        updateForm {
            it.copy(genres = emptySet())
        }
    }

    fun onResetSorting() {
        updateForm {
            it.copy(sorting = null)
        }
    }

    fun onResetYears() {
        updateForm {
            it.copy(years = emptySet())
        }
    }

    fun onAgeRatings(item: FormItem.Value) {
        updateForm {
            it.copy(ageRatings = it.ageRatings.toggle(item))
        }
    }

    fun onGenre(item: FormItem.Genre) {
        updateForm {
            it.copy(genres = it.genres.toggle(item))
        }
    }

    fun onPublishStatus(item: FormItem.Value) {
        updateForm {
            it.copy(publishStatuses = it.publishStatuses.toggle(item))
        }
    }

    fun onProductionStatus(item: FormItem.Value) {
        updateForm {
            it.copy(productionStatuses = it.productionStatuses.toggle(item))
        }
    }

    fun onReleaseType(item: FormItem.Value) {
        updateForm {
            it.copy(types = it.types.toggle(item))
        }
    }

    fun onSeason(item: FormItem.Value) {
        updateForm {
            it.copy(seasons = it.seasons.toggle(item))
        }
    }

    fun onSorting(item: FormItem.Value) {
        updateForm {
            val newSorting = if (it.sorting == item) {
                null
            } else {
                item
            }
            it.copy(sorting = newSorting)
        }
    }

    fun onYear(item: FormItem.Year) {
        updateForm {
            it.copy(years = it.years.toggle(item))
        }
    }

    fun onYears(item: Pair<FormItem.Year, FormItem.Year>) {
        updateForm {
            it.copy(yearsRange = item)
        }
    }

    private fun <T : FormItem> Set<T>.toggle(item: T): Set<T> {
        return if (contains(item)) {
            minus(item)
        } else {
            plus(item)
        }
    }

    private fun updateForm(block: (FilterForm) -> FilterForm) {
        _state.update {
            it.copy(form = block(it.form))
        }
    }
}