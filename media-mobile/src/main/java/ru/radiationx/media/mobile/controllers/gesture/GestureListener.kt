package ru.radiationx.media.mobile.controllers.gesture

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration

internal class GestureListener : GestureDetector.SimpleOnGestureListener() {

    private var lastDoubleTapTime = 0L
    private val doubleTapDelay = ViewConfiguration.getDoubleTapTimeout() * 2
    private val tapDelay = ViewConfiguration.getTapTimeout()

    var scrollAllowProvider: (() -> Boolean)? = null

    var singleTapListener: (() -> Unit)? = null
    var doubleTapListener: ((event: MotionEvent) -> Unit)? = null
    var scrollListener: ((deltaX: Float, eventId: Long) -> Unit)? = null
    var onLongPress: (() -> Unit)? = null

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val delta = e.downTime - lastDoubleTapTime
        if (delta <= doubleTapDelay) {
            lastDoubleTapTime = e.downTime
            doubleTapListener?.invoke(e)
            return true
        }
        return super.onSingleTapUp(e)
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        val delta = e.downTime - lastDoubleTapTime
        if (delta <= doubleTapDelay) {
            if (delta > tapDelay) {
                lastDoubleTapTime = e.downTime
                doubleTapListener?.invoke(e)
            }
        } else {
            singleTapListener?.invoke()
        }
        return super.onSingleTapConfirmed(e)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        lastDoubleTapTime = e.downTime
        doubleTapListener?.invoke(e)
        return super.onDoubleTap(e)
    }

    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
        onLongPress?.invoke()
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float,
    ): Boolean {
        if (e1 == null) {
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
        if (scrollAllowProvider?.invoke() == true) {
            return false
        }
        val deltaX = e2.x - e1.x
        scrollListener?.invoke(deltaX, e1.downTime)
        return super.onScroll(e1, e2, distanceX, distanceY)
    }
}