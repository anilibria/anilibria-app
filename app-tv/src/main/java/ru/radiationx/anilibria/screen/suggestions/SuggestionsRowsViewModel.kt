package ru.radiationx.anilibria.screen.suggestions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.common.BaseRowsViewModel
import toothpick.InjectConstructor

@InjectConstructor
class SuggestionsRowsViewModel(
    private val suggestionsController: SuggestionsController
) : BaseRowsViewModel() {

    companion object {
        const val RESULT_ROW_ID = 1L
        const val RECOMMENDS_ROW_ID = 2L
    }

    val emptyResultState = MutableLiveData<Boolean>()

    override val rowIds: List<Long> = listOf(RESULT_ROW_ID, RECOMMENDS_ROW_ID)

    override val availableRows: MutableSet<Long> = mutableSetOf(RECOMMENDS_ROW_ID)

    init {
        suggestionsController
            .resultEvent
            .onEach {
                emptyResultState.value = it.validQuery && it.items.isEmpty()
                updateAvailableRow(RESULT_ROW_ID, it.validQuery && it.items.isNotEmpty())
                updateAvailableRow(RECOMMENDS_ROW_ID, !it.validQuery && it.items.isEmpty())
            }
            .launchIn(viewModelScope)
    }

}