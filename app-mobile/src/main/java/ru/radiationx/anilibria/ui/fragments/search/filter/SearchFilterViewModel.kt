package ru.radiationx.anilibria.ui.fragments.search.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.data.api.shared.filter.FilterForm
import ru.radiationx.data.api.shared.filter.FormItem
import javax.inject.Inject

class SearchFilterViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(FilterForm.empty())
    val state = _state.asStateFlow()

    private val _applyEvent = MutableSharedFlow<FilterForm>()
    val applyEvent = _applyEvent.asSharedFlow()

    fun onApply() {
        viewModelScope.launch {
            _applyEvent.emit(state.value)
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
        _state.update(block)
    }
}