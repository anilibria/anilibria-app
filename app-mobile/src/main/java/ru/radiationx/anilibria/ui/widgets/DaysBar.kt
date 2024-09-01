package ru.radiationx.anilibria.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.databinding.MergeDaysBarBinding
import java.util.Calendar

class DaysBar @JvmOverloads constructor(
    context: Context,
    attrs:
    AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding by viewBinding<MergeDaysBarBinding>(attachToRoot = true)

    private val daysViews: Map<Int, View> = mapOf(
        Calendar.MONDAY to binding.day1,
        Calendar.TUESDAY to binding.day2,
        Calendar.WEDNESDAY to binding.day3,
        Calendar.THURSDAY to binding.day4,
        Calendar.FRIDAY to binding.day5,
        Calendar.SATURDAY to binding.day6,
        Calendar.SUNDAY to binding.day7
    )
    private val buttons: List<View> = daysViews.values.toList()

    var clickListener: ((day: Int) -> Unit)? = null

    init {
        buttons.forEach { view ->
            view.setOnClickListener { btn ->
                val day = daysViews.entries.firstOrNull { it.value == btn }?.key
                day?.also {
                    clickListener?.invoke(it)
                }
            }
        }
    }

    fun selectDay(day: Int) {
        buttons.forEach { it.isSelected = false }
        daysViews[day]?.isSelected = true
    }

}
