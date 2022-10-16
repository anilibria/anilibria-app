package ru.radiationx.anilibria.utils

import android.util.Log
import android.view.View

/**
 * Created by radiationx on 30.12.17.
 */
class DimensionHelper(
        private var measurer: View?,
        private var container: View?,
        private var listener: DimensionsListener?
) {

    private val dimension = Dimensions()

    private var lastSb = 0
    private var lastNb = 0
    private var lastCh = 0
    private var lastKh = 0

    private val layoutListener = View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        val container = this.container ?: return@OnLayoutChangeListener
        val listener = this.listener ?: return@OnLayoutChangeListener
        var anyChanges = false
        if (dimension.contentHeight == 0) {
            dimension.statusBar = v.top
            dimension.navigationBar = container.bottom - v.bottom
        }

        dimension.contentHeight = v.height
        dimension.keyboardHeight = container.height - dimension.contentHeight - dimension.statusBar - dimension.navigationBar

        dimension.let {
            if (it.statusBar != lastSb
                    || it.navigationBar != lastNb
                    || it.contentHeight != lastCh
                    || it.keyboardHeight != lastKh) {

                lastSb = it.statusBar
                lastNb = it.navigationBar
                lastCh = it.contentHeight
                lastKh = it.keyboardHeight
                listener.onDimensionsChange(it)
            }
        }
    }

    init {
        measurer?.addOnLayoutChangeListener(layoutListener)
    }

    fun destroy() {
        measurer?.removeOnLayoutChangeListener(layoutListener)
        measurer = null
        container = null
        listener = null
    }

    data class Dimensions(
            var statusBar: Int = 0,
            var navigationBar: Int = 0,
            var contentHeight: Int = 0,
            var keyboardHeight: Int = 0
    ) {
        override fun toString(): String {
            return "Dimensions:\nto=$statusBar\nbo=$navigationBar\nch=$contentHeight\nkh=$keyboardHeight"
        }
    }

    interface DimensionsListener {
        fun onDimensionsChange(dimensions: Dimensions)
    }
}