package ru.radiationx.anilibria.ui.fragments.search.filter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.radiationx.data.api.shared.filter.FilterForm
import javax.inject.Inject

class SearchFilterViewModel @Inject constructor(

) : ViewModel() {

    private val _state = MutableStateFlow(FilterForm.empty())
    val state = _state.asStateFlow()

    fun onNewForm(form: FilterForm) {
        _state.value = form
    }
}