package ru.radiationx.anilibria.common

import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.screen.LifecycleViewModel

abstract class BaseRowsViewModel : LifecycleViewModel() {

    val rowListData = MutableStateFlow<List<Long>>(emptyList())

    protected abstract val rowIds: List<Long>

    protected abstract val availableRows: MutableSet<Long>

    override fun onCreate() {
        super.onCreate()
        updateRows()
    }

    protected fun updateAvailableRow(rowId: Long, available: Boolean) {
        if (available) {
            availableRows.add(rowId)
        } else {
            availableRows.remove(rowId)
        }
        updateRows()
    }

    protected fun updateRows() {
        rowListData.value = getRows()
    }

    protected fun getRows(): List<Long> =
        rowIds.toMutableList().filter { availableRows.contains(it) }

}