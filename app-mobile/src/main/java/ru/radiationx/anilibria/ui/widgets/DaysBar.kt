package ru.radiationx.anilibria.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.databinding.MergeDaysBarBinding
import ru.radiationx.data.api.schedule.models.PublishDay

class DaysBar @JvmOverloads constructor(
    context: Context,
    attrs:
    AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding by viewBinding<MergeDaysBarBinding>(attachToRoot = true)

    private val daysViews: Map<PublishDay, View> = mapOf(
        PublishDay.Monday to binding.day1,
        PublishDay.Tuesday to binding.day2,
        PublishDay.Wednesday to binding.day3,
        PublishDay.Thursday to binding.day4,
        PublishDay.Friday to binding.day5,
        PublishDay.Saturday to binding.day6,
        PublishDay.Sunday to binding.day7
    )
    private val buttons: List<View> = daysViews.values.toList()

    var clickListener: ((day: PublishDay) -> Unit)? = null

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

    fun selectDay(day: PublishDay) {
        buttons.forEach { it.isSelected = false }
        daysViews[day]?.isSelected = true
    }

}
