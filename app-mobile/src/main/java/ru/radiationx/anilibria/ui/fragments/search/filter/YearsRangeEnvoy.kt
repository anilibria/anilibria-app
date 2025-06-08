package ru.radiationx.anilibria.ui.fragments.search.filter

import com.google.android.material.slider.RangeSlider
import envoy.DiffItem
import envoy.ext.viewBindingEnvoy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.anilibria.databinding.ItemFilterYearsBinding
import ru.radiationx.anilibria.utils.view.attachedCoroutineScope
import ru.radiationx.data.api.shared.filter.FilterItem
import ru.radiationx.data.api.shared.filter.FormItem
import kotlin.math.roundToInt

data class YearsRangeState(
    val years: List<FilterItem.Year>,
    val selected: Pair<FormItem.Year, FormItem.Year>?
) : DiffItem("years")

fun yearsRangeEnvoy(
    yearsListener: (Pair<FormItem.Year, FormItem.Year>) -> Unit
) = viewBindingEnvoy<YearsRangeState, ItemFilterYearsBinding> {

    val yearsFlow = MutableSharedFlow<Pair<FormItem.Year, FormItem.Year>>()

    fun RangeSlider.getYearsRange(): Pair<FormItem.Year, FormItem.Year>? {
        val valueFrom = values.firstOrNull()
        val valueTo = values.lastOrNull()
        if (valueFrom == null || valueTo == null) {
            return null
        }
        return FormItem.Year(valueFrom.roundToInt()) to FormItem.Year(valueTo.roundToInt())
    }

    view.seekbar.apply {
        valueFrom = 1990f
        valueTo = 2025f
        values = listOf(valueFrom, valueTo)
        stepSize = 1f

        addOnChangeListener { slider, _, _ ->
            val yearsRange = slider.getYearsRange() ?: return@addOnChangeListener
            view.yearFrom.text = yearsRange.first.year.toString()
            view.yearTo.text = yearsRange.second.year.toString()
            view.root.attachedCoroutineScope.launch {
                yearsFlow.emit(yearsRange)
            }
        }
    }

    attach {
        yearsFlow
            .debounce(300)
            .onEach { yearsListener.invoke(it) }
            .launchIn(view.root.attachedCoroutineScope)
    }

    bind { state ->
        view.seekbar.valueFrom = state.years.minOf { it.item.year }.toFloat()
        view.seekbar.valueTo = state.years.maxOf { it.item.year }.toFloat()
        view.seekbar.values = state.selected
            ?.let { listOf(it.first.year.toFloat(), it.second.year.toFloat()) }
            ?: listOf(view.seekbar.valueFrom, view.seekbar.valueTo)
    }
}