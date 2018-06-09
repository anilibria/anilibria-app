package ru.radiationx.anilibria.ui.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import com.devbrackets.android.exomedia.ui.animation.BottomViewHideShowAnimation
import com.devbrackets.android.exomedia.ui.animation.TopViewHideShowAnimation
import com.devbrackets.android.exomedia.ui.widget.VideoControls
import com.devbrackets.android.exomedia.ui.widget.VideoControlsMobile
import kotlinx.android.synthetic.main.view_video_control.view.*
import ru.radiationx.anilibria.R
import android.support.graphics.drawable.ArgbEvaluator
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MotionEvent
import ru.radiationx.anilibria.extension.asTimeSecString
import ru.radiationx.anilibria.ui.widgets.gestures.VideoGestureEventsListener
import java.lang.Math.pow
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue


class VideoControlsAlib(context: Context) : VideoControlsMobile(context) {

    private var alibControlsListener: AlibControlsListener? = null

    fun setOpeningListener(listener: AlibControlsListener) {
        alibControlsListener = listener
    }


    override fun getLayoutResource() = R.layout.view_video_control

    override fun retrieveViews() {
        super.retrieveViews()
        controlsContainer = timeControlsContainer
        gesturesControllerView.setEventsListener(object : VideoGestureEventsListener {
            private var localSeekDelta = 0L
            private var seekStarted = false;

            override fun onTap() {
                Log.e("gestureLalala", "onTap")
            }

            override fun onHorizontalScroll(event: MotionEvent?, delta: Float) {
                if (!seekStarted) {
                    gestureSeekValue.visibility = View.VISIBLE
                    seekStarted = true
                }

                val duration = videoView?.duration ?: 0
                val currentPosition = videoView?.currentPosition ?: 0
                val percent: Int = ((delta / gesturesControllerView.width) * 100).toInt()
                val seconds = (pow(percent.toDouble(), 2.0) / 25).toLong() * if (percent < 0) -1 else 1
                val seekMillis = TimeUnit.SECONDS.toMillis(seconds)
                val targetPosition = (currentPosition + seekMillis).coerceIn(0, duration)

                val textValue = "${if (seekMillis > 0) "+" else "-"}${Date(seekMillis.absoluteValue).asTimeSecString()}"

                gestureSeekValue.text = textValue
                localSeekDelta = seekMillis

                Log.e("gestureLalala", "onHorizontalScroll, d=$delta, p=$percent, s=$seekMillis tv=$textValue, tp=$targetPosition")
            }

            override fun onVerticalScroll(event: MotionEvent?, delta: Float) {
                Log.e("gestureLalala", "onVerticalScroll, d=$delta, e=${event?.action}")
            }

            override fun onSwipeRight() {
                Log.e("gestureLalala", "onSwipeRight")
            }

            override fun onSwipeLeft() {
                Log.e("gestureLalala", "onSwipeLeft")
            }

            override fun onSwipeBottom() {
                Log.e("gestureLalala", "onSwipeBottom")
            }

            override fun onSwipeTop() {
                Log.e("gestureLalala", "onSwipeTop")
            }

            override fun onStart() {
                Log.e("gestureLalala", "onStart")
            }

            override fun onEnd() {
                Log.e("gestureLalala", "onEnd, lsd=$localSeekDelta")
                videoView?.apply {
                    if (localSeekDelta != 0L) {
                        seekTo((currentPosition + localSeekDelta).coerceIn(0, duration))
                    }
                }
                gestureSeekValue.visibility = View.GONE
                seekStarted = false
                localSeekDelta = 0
            }
        })
    }

    override fun registerListeners() {
        super.registerListeners()
        controlMinusOpening.setOnClickListener { alibControlsListener?.onMinusClick() }
        controlPlusOpening.setOnClickListener { alibControlsListener?.onPlusClick() }
        controlsFullscreen.setOnClickListener { alibControlsListener?.onFullScreenClick() }
    }

    override fun animateVisibility(toVisible: Boolean) {
        if (isVisible == toVisible) {
            return
        }

        if (!hideEmptyTextContainer || !isTextContainerEmpty) {
            textContainer.startAnimation(TopViewHideShowAnimation(textContainer, toVisible, VideoControls.CONTROL_VISIBILITY_ANIMATION_LENGTH))
        }

        if (!isLoading) {
            controlsContainer.startAnimation(BottomViewHideShowAnimation(controlsContainer, toVisible, VideoControls.CONTROL_VISIBILITY_ANIMATION_LENGTH))
            controlButtonsWrapper.startAnimation(CenterViewHideShowAnimation(controlButtonsWrapper, toVisible, 225))

            val colorDark = Color.argb(127, 0, 0, 0)
            val colorFrom = if (toVisible) Color.TRANSPARENT else colorDark
            val colorTo = if (!toVisible) Color.TRANSPARENT else colorDark
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
            colorAnimation.duration = 335 // milliseconds
            colorAnimation.addUpdateListener { animator -> videoControlsRoot.setBackgroundColor(animator.animatedValue as Int) }
            colorAnimation.start()
        }

        isVisible = toVisible
        onVisibilityChanged()
    }

    override fun showLoading(initialLoad: Boolean) {
        if (isLoading) {
            return
        }

        isLoading = true
        loadingProgressBar.visibility = View.VISIBLE

        if (initialLoad) {
            controlsContainer.visibility = View.GONE
            controlButtonsWrapper.visibility = View.GONE
        } else {
            playPauseButton.isEnabled = false
            previousButton.isEnabled = false
            nextButton.isEnabled = false
        }

        show()
    }

    override fun finishLoading() {
        if (!isLoading) {
            return
        }

        isLoading = false
        loadingProgressBar.visibility = View.GONE
        controlsContainer.visibility = View.VISIBLE
        controlButtonsWrapper.visibility = View.VISIBLE

        playPauseButton.isEnabled = true
        previousButton.isEnabled = enabledViews.get(com.devbrackets.android.exomedia.R.id.exomedia_controls_previous_btn, true)
        nextButton.isEnabled = enabledViews.get(com.devbrackets.android.exomedia.R.id.exomedia_controls_next_btn, true)

        updatePlaybackState(videoView != null && videoView!!.isPlaying)
    }

    fun setFullScreenMode(isFullscreen: Boolean) {
        val icRes = if (isFullscreen) {
            R.drawable.ic_fullscreen_exit
        } else {
            R.drawable.ic_fullscreen
        }
        controlsFullscreen.apply {
            setImageDrawable(ContextCompat.getDrawable(context, icRes))
        }
    }

    interface AlibControlsListener {
        fun onMinusClick()
        fun onPlusClick()
        fun onFullScreenClick()
    }
}