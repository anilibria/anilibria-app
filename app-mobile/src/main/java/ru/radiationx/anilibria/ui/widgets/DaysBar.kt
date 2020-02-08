package ru.radiationx.anilibria.ui.widgets

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.merge_days_bar.view.*
import ru.radiationx.anilibria.R
import java.util.*

class DaysBar @JvmOverloads constructor(
        context: Context, attrs:
        AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val daysViews: Map<Int, View>
    private val buttons: List<View>

    var clickListener: ((day: Int) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.merge_days_bar, this, true)

        daysViews = mapOf(
                Calendar.MONDAY to day1,
                Calendar.TUESDAY to day2,
                Calendar.WEDNESDAY to day3,
                Calendar.THURSDAY to day4,
                Calendar.FRIDAY to day5,
                Calendar.SATURDAY to day6,
                Calendar.SUNDAY to day7
        )
        buttons = daysViews.values.toList()

        buttons.forEach {
            it.setOnClickListener { btn ->
                val day = daysViews.entries.firstOrNull { it.value == btn }?.key
                day?.also {
                    clickListener?.invoke(it)
                }
            }
        }
    }

    fun selectDays(days: List<Int>) {
        buttons.forEach { it.isSelected = false }
        days.forEach { day ->
            daysViews[day]?.isSelected = true
        }
    }

}
