package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.databinding.ViewCardDescriptionBinding

class CardDescriptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private val binding by viewBinding<ViewCardDescriptionBinding>(attachToRoot = true)

    init {
        orientation = VERTICAL
    }

    fun setTitle(title: CharSequence) {
        binding.cardDescriptionTitle.text = title
    }

    fun setSubtitle(subtitle: CharSequence) {
        binding.cardDescriptionSubtitle.text = subtitle
    }

    fun isFilled(): Boolean {
        return binding.cardDescriptionTitle.text.isNotEmpty() || binding.cardDescriptionSubtitle.text.isNotEmpty()
    }
}