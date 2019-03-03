package ru.radiationx.anilibria.utils

import android.util.Log
import android.view.View

/**
 * Created by radiationx on 30.12.17.
 */
class DimensionHelper(
        measurer: View,
        private val container: View,
        private val listener: DimensionsListener
) {

    private val dimension = Dimensions()

    private var lastSb = 0
    private var lastNb = 0
    private var lastCh = 0
    private var lastKh = 0

    init {
        measurer.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            Log.e("S_DEF_LOG", "OnLayoutChange $left $top $right $bottom ||| $oldLeft $oldTop $oldRight $oldBottom")
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