package ru.radiationx.anilibria.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_card_loading.view.*
import ru.radiationx.anilibria.R

class CardLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_card_loading, this)
        setBackgroundResource(R.color.dark_colorPrimary)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    fun setState(state: State) {
        loadingProgressBar.isVisible = state == State.LOADING
        loadingRefresh.isVisible = state == State.REFRESH
    }

    enum class State {
        LOADING,
        REFRESH
    }
}