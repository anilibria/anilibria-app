package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_shadow_description.view.*
import ru.radiationx.anilibria.R

class ShadowDescriptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_shadow_description, this)
        setBackgroundResource(R.drawable.bg_grid_description_shadow)
    }

    fun setDescription(title: CharSequence, subtitle: CharSequence) {
        cardDescriptionView.apply {
            setTitle(title)
            setSubtitle(subtitle)
        }
    }

    fun getCardDescriptionView(): CardDescriptionView = cardDescriptionView
}