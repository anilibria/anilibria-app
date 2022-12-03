package ru.radiationx.anilibria.ui.widget.manager

import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

abstract class ExternalViewManager {

    companion object {
        private const val DEFAULT_SHOWING_DELAY: Long = 500
    }

    var initialDelay = DEFAULT_SHOWING_DELAY

    var rootView: ViewGroup? = null

    protected var externalView: View? = null
        private set

    private var userProvidedView = false
    private var isEnabled = true
    private var isShowing = false

    private val handler = Handler()

    private val showingRunnable = Runnable {
        if (!isEnabled || !userProvidedView && rootView == null) {
            return@Runnable
        }
        if (isShowing) {
            if (externalView == null) {
                externalView = createView()
                val progressBarParams = createLayoutParams()
                rootView?.addView(externalView, progressBarParams)
            } else if (userProvidedView) {
                externalView?.isVisible = true
            }
            onApplyShowing()
        }
    }

    open fun setCreatedView(view: View) {
        requireNotNull(view.parent) { "Must have a parent" }
        externalView = view
        externalView?.isInvisible = true
        userProvidedView = true
    }

    open fun show() {
        if (isEnabled) {
            isShowing = true
            handler.postDelayed(showingRunnable, initialDelay)
        }
    }

    open fun hide() {
        isShowing = false
        if (userProvidedView) {
            externalView?.isInvisible = true
        } else if (externalView != null) {
            rootView?.removeView(externalView)
            externalView = null
        }
        handler.removeCallbacks(showingRunnable)
    }

    open fun disableView() {
        isEnabled = false
    }

    open fun enableView() {
        isEnabled = true
    }

    protected open fun onApplyShowing() {}

    protected abstract fun createView(): View

    protected abstract fun createLayoutParams(): ViewGroup.LayoutParams
}