package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import kotlinx.android.synthetic.main.view_card_description.view.*
import ru.radiationx.anilibria.R

class CardDescriptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_card_description, this)
        orientation = VERTICAL
    }

    fun setTitle(title: CharSequence) {
        cardDescriptionTitle.text = title
    }

    fun setSubtitle(subtitle: CharSequence) {
        cardDescriptionSubtitle.text = subtitle
    }

    fun isFilled(): Boolean = cardDescriptionTitle.text.isNotEmpty() || cardDescriptionSubtitle.text.isNotEmpty()
}