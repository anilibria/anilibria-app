package ru.radiationx.anilibria.screen.search

import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.screen.LifecycleViewModel

abstract class BaseSearchValuesViewModel(
    private val argExtra: SearchValuesExtra
) : LifecycleViewModel() {

    val progressState = MutableStateFlow<Boolean>(false)
    val valuesData = MutableStateFlow<List<String>>(emptyList())
    val checkedIndicesData = MutableStateFlow<List<Pair<Int, Boolean>>>(emptyList())
    val selectedIndex = MutableStateFlow<Int?>(null)

    protected val currentValues = mutableListOf<String>()
    protected val checkedValues = mutableSetOf<String>()

    init {
        checkedValues.addAll(argExtra.values)
        updateChecked()
        updateSelected()
    }

    abstract fun applyValues()

    fun resetSelected() {
        checkedValues.clear()
        updateChecked()
    }

    fun setSelected(index: Int, selected: Boolean) {
        val value = currentValues[index]
        if (selected) {
            checkedValues.add(value)
        } else {
            checkedValues.remove(value)
        }
        updateChecked()
    }

    protected fun updateSelected() {
        if (currentValues.isEmpty() || checkedValues.isEmpty()) {
            return
        }
        val firstCheckedValue = currentValues.firstOrNull { checkedValues.contains(it) }
        firstCheckedValue?.also {
            selectedIndex.value = currentValues.indexOf(it)
        }
    }

    protected fun updateChecked() {
        checkedIndicesData.value =
            currentValues.mapIndexed { index, item -> Pair(index, checkedValues.contains(item)) }
    }
}