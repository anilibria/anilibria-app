package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ViewCardLoadingBinding

class CardLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding by viewBinding<ViewCardLoadingBinding>(attachToRoot = true)

    init {
        setBackgroundResource(R.color.dark_colorPrimary)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    fun setState(state: State) {
        binding.loadingProgressBar.isVisible = state == State.LOADING
        binding.loadingRefresh.isVisible = state == State.ERROR
    }

    enum class State {
        LOADING,
        ERROR
    }
}