package ru.radiationx.media.mobile.controllers.gesture

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

internal class GestureListener(
    private val viewConfiguration: ViewConfiguration,
) : GestureDetector.SimpleOnGestureListener() {

    private val touchSlop = viewConfiguration.scaledTouchSlop
    private var scrollAxis = ScrollAxis.None
    private var lastDoubleTapTime = 0L
    private val doubleTapDelay = ViewConfiguration.getDoubleTapTimeout() * 2
    private val tapDelay = ViewConfiguration.getTapTimeout()

    var scrollAllowProvider: (() -> Boolean)? = null

    var singleTapListener: (() -> Unit)? = null
    var doubleTapListener: ((event: MotionEvent) -> Unit)? = null
    var scrollListener: ((deltaX: Float, eventId: Long) -> Unit)? = null
    var onLongPress: (() -> Unit)? = null

    fun onTouchEnd() {
        scrollAxis = ScrollAxis.None
    }

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
        val deltaY = e2.y - e1.y

        if (scrollAxis == ScrollAxis.None) {
            when {
                abs(deltaX) >= touchSlop -> scrollAxis = ScrollAxis.X
                abs(deltaY) >= touchSlop -> scrollAxis = ScrollAxis.Y
            }
        }
        if (scrollAxis == ScrollAxis.X) {
            scrollListener?.invoke(deltaX, e1.downTime)
        }
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    enum class ScrollAxis {
        X,
        Y,
        None
    }
}