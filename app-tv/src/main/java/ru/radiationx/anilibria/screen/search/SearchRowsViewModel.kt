package ru.radiationx.anilibria.screen.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.BaseRowsViewModel
import toothpick.InjectConstructor

@InjectConstructor
class SearchRowsViewModel(
    private val searchController: SearchController
) : BaseRowsViewModel() {

    companion object {
        const val RESULT_ROW_ID = 1L
        const val RECOMMENDS_ROW_ID = 2L
    }

    val emptyResultState = MutableLiveData<Boolean>()

    override val rowIds: List<Long> = listOf(RESULT_ROW_ID, RECOMMENDS_ROW_ID)

    override val availableRows: MutableSet<Long> = mutableSetOf(RECOMMENDS_ROW_ID)

    override fun onColdCreate() {
        super.onColdCreate()

        searchController
            .resultEvent
            .lifeSubscribe {
                Log.e("kokoko", "resultEvent $it")
                emptyResultState.value = it.validQuery && it.items.isEmpty()
                updateAvailableRow(RESULT_ROW_ID, it.validQuery && it.items.isNotEmpty())
                updateAvailableRow(RECOMMENDS_ROW_ID, !it.validQuery && it.items.isEmpty())
            }
    }

}