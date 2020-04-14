package ru.radiationx.anilibria.ui.widget.manager

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar

open class ExternalProgressManager : ExternalViewManager() {

    override fun createView(): View {
        return ProgressBar(rootView?.context, null, android.R.attr.progressBarStyleLarge)
    }

    override fun createLayoutParams(): ViewGroup.LayoutParams {
        val centerLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        centerLayoutParams.gravity = Gravity.CENTER
        return centerLayoutParams
    }
}