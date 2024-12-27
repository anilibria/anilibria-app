package ru.radiationx.media.mobile.controllers.gesture

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.holder.PlayerAttachListener
import ru.radiationx.media.mobile.utils.TimeFormatter

@SuppressLint("ClickableViewAccessibility")
internal class GestureController(
    private val playerFlow: PlayerFlow,
    private val coroutineScope: CoroutineScope,
    private val fallbackTapView: View,
    private val gestureView: View,
    private val seekerTime: TextView,
    private val mediaAspectRatio: View,
) : PlayerAttachListener {

    private val gestureListener = GestureListener(ViewConfiguration.get(gestureView.context))
    private val gestureDetector = GestureDetector(gestureView.context, gestureListener)

    // todo use for youtube scale gestures
    private val scaleDetector = ScaleGestureDetector(
        gestureView.context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            private var scaleDiff = 0f

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                val currentViewScale = maxOf(mediaAspectRatio.scaleX, mediaAspectRatio.scaleY)
                scaleDiff = currentViewScale - 1f
                _liveScale.value = detector.scaleFactor + scaleDiff
                return super.onScaleBegin(detector)
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                _liveScale.value = detector.scaleFactor + scaleDiff
                return super.onScale(detector)
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                _liveScale.value = null
                scaleDiff = 0f
                super.onScaleEnd(detector)
            }
        }
    )

    private val doubleTapSeeker = DoubleTapSeeker(playerFlow, coroutineScope, gestureView)
    private val scrollSeeker = ScrollSeeker(playerFlow, gestureView)
    private val longTapSeeker = LongTapSeeker()

    var singleTapListener: (() -> Unit)? = null

    private val _liveScale = MutableStateFlow<Float?>(null)
    val liveScale = _liveScale.asStateFlow()

    val doubleTapSeekerState = doubleTapSeeker.state
    val scrollSeekerState = scrollSeeker.state
    val longTapSeekerState = longTapSeeker.state

    init {
        gestureListener.scrollAllowProvider = { scaleDetector.isInProgress }

        gestureListener.singleTapListener = {
            singleTapListener?.invoke()
        }
        fallbackTapView.setOnClickListener {
            singleTapListener?.invoke()
        }
        gestureListener.doubleTapListener = {
            doubleTapSeeker.onDoubleTap(it)
        }
        gestureListener.scrollListener = { deltaX, eventId ->
            scrollSeeker.onScroll(deltaX, eventId)
        }
        gestureListener.onLongPress = {
            longTapSeeker.onOnLongTap()
        }

        gestureView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    scrollSeeker.setIgnore(null)
                }
            }
            var result = scaleDetector.onTouchEvent(event)
            if (scaleDetector.isInProgress) {
                scrollSeeker.setIgnore(event.downTime)
            } else {
                result = gestureDetector.onTouchEvent(event) || result
            }
            when (event.action) {
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL,
                    -> {
                    scrollSeeker.onTouchEnd(event.downTime)
                    scrollSeeker.setIgnore(null)
                    longTapSeeker.onTouchEnd()
                    gestureListener.onTouchEnd()
                }
            }
            result
        }

        doubleTapSeeker.applyListener = {
            applySeekerState(it)
        }

        scrollSeeker.applyListener = {
            applySeekerState(it)
        }

        doubleTapSeeker.state.onEach {
            seekerTime.text = TimeFormatter.format(it.deltaSeek, true)
        }.launchIn(coroutineScope)

        scrollSeeker.state.onEach {
            seekerTime.text = TimeFormatter.format(it.deltaSeek, true)
        }.launchIn(coroutineScope)

        longTapSeeker.state.onEach {
            seekerTime.text = "2x"
        }.launchIn(coroutineScope)
    }

    private fun applySeekerState(state: SeekerState) {
        if (state.deltaSeek == 0L) {
            return
        }
        playerFlow.seekTo(state.initialSeek + state.deltaSeek)
    }
}

