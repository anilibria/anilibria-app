package ru.radiationx.anilibria.screen.search

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.screen.LifecycleViewModel

abstract class BaseSearchValuesViewModel(
    private val argExtra: SearchValuesExtra
) : LifecycleViewModel() {

    val progressState = MutableLiveData<Boolean>()
    val valuesData = MutableLiveData<List<String>>()
    val checkedIndicesData = MutableLiveData<List<Pair<Int, Boolean>>>()
    val selectedIndex = MutableLiveData<Int>()

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