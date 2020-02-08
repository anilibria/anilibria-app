package ru.radiationx.anilibria.ui.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.support.graphics.drawable.ArgbEvaluator
import android.util.AttributeSet
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import com.devbrackets.android.exomedia.ui.animation.BottomViewHideShowAnimation
import com.devbrackets.android.exomedia.ui.animation.TopViewHideShowAnimation
import com.devbrackets.android.exomedia.ui.widget.VideoControls
import com.devbrackets.android.exomedia.ui.widget.VideoControlsMobile
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.view_video_control.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.*
import ru.radiationx.anilibria.ui.widgets.gestures.VideoGestureEventsListener
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared.ktx.asTimeSecString
import java.lang.Math.pow
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue


class VideoControlsAlib @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : VideoControlsMobile(context, attrs, defStyleAttr) {

    private var alibControlsListener: AlibControlsListener? = null
    private var pictureInPictureMenuItem: MenuItem? = null
    private var controlsEnabled = true

    fun setOpeningListener(listener: AlibControlsListener) {
        alibControlsListener = listener
    }

    fun fitSystemWindows(fit: Boolean) {
        this.fitsSystemWindows = false
        videoControlsRoot.fitsSystemWindows = fit
    }

    fun setPictureInPictureEnabled(enabled: Boolean) {
        pictureInPictureMenuItem?.isVisible = enabled
    }

    /*override fun updateProgress(position: Long, duration: Long, bufferPercent: Int) {
        super.updateProgress(position, duration, bufferPercent)
        val percent = position.toFloat() / duration.toFloat()
        controlMinusOpening.visible(percent < 0.3)
        controlPlusOpening.visible(percent < 0.3)
    }*/

    override fun getLayoutResource() = R.layout.view_video_control

    override fun retrieveViews() {
        super.retrieveViews()
        textContainer = appbarLayout

        appbarLayout.apply {
            background = context.getCompatDrawable(R.drawable.bg_video_toolbar)
        }

        toolbar.apply {
            navigationIcon = context.getCompatDrawable(R.drawable.ic_toolbar_arrow_back)
            setNavigationOnClickListener {
                alibControlsListener?.onBackClick()
            }
            pictureInPictureMenuItem = toolbar.menu.add("Картинка в картинке")
                    .setIcon(context.getCompatDrawable(R.drawable.ic_picture_in_picture_alt_toolbar))
                    .setOnMenuItemClickListener {
                        alibControlsListener?.onPIPClick()
                        true
                    }
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        controlsContainer = timeControlsContainer
        gesturesControllerView.setEventsListener(object : VideoGestureEventsListener {
            private var localSeekDelta = 0L
            private var swipeSeekStarted = false
            private var tapSeekStarted = false

            private var tapDisposable = Disposables.disposed()
            private val tapSeekHandler = Handler()
            private val tapSeekRunnable = Runnable {
                applyPlayerSeek()
                gestureSeekValue.gone()
                tapDisposable.dispose()
                tapSeekStarted = false
                localSeekDelta = 0
            }

            private val tapRelay = PublishRelay.create<MotionEvent>()

            private fun handleEndSwipeSeek() {
                applyPlayerSeek()
                gestureSeekValue.gone()
                gesturesControllerView.background = null
                swipeSeekStarted = false
                localSeekDelta = 0
            }

            private fun handleStartTapSeek() {
                tapDisposable.dispose()
                tapDisposable = tapRelay
                        //.debounce(100L, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { handleTapSeek(it) }
            }

            private fun handleTapSeek(event: MotionEvent?) {
                val xPos = (event?.x ?: 0f).toInt()
                val seconds = if (xPos > gesturesControllerView.width / 2) 10L else -10L
                val seekMillis = TimeUnit.SECONDS.toMillis(seconds)

                localSeekDelta += seekMillis

                val textValue = "${if (localSeekDelta > 0) "+" else "-"}${Date(localSeekDelta.absoluteValue).asTimeSecString()}"
                gestureSeekValue.text = textValue
            }

            private fun handleEndTapSeek() {
                tapSeekHandler.removeCallbacks(tapSeekRunnable)
                tapSeekHandler.postDelayed(tapSeekRunnable, 350)
            }

            private fun applyPlayerSeek() {
                videoView?.apply {
                    if (localSeekDelta != 0L) {
                        seekTo((currentPosition + localSeekDelta).coerceIn(0, duration))
                    }
                }
            }

            override fun onTap(event: MotionEvent?) {
                Log.e("gestureLalala", "onTap, $canViewHide, $isVisible, $tapSeekStarted")

                videoView?.showControls()
                if (tapSeekStarted) {
                    event?.also { tapRelay.accept(it) }
                }
            }

            override fun onDoubleTap(event: MotionEvent?) {
                Log.e("gestureLalala", "onDoubleTap,;;; ${event?.x}:${event?.y};;;;  $canViewHide, $isVisible")

                if (!tapSeekStarted) {
                    gestureSeekValue.visible()
                    tapSeekStarted = true
                    handleStartTapSeek()
                }
                event?.also { tapRelay.accept(it) }
            }

            override fun onHorizontalScroll(event: MotionEvent?, delta: Float) {
                if (!swipeSeekStarted) {
                    gestureSeekValue.visible()
                    swipeSeekStarted = true
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
                if (swipeSeekStarted) {
                    handleEndSwipeSeek()
                }
                if (tapSeekStarted) {
                    handleEndTapSeek()
                }
            }


        })
    }

    override fun updatePlaybackState(isPlaying: Boolean) {
        super.updatePlaybackState(isPlaying)
        Log.e("kulolo", "updatePlaybackState $isPlaying")
        if (!isPlaying && !isLoading) {
            hideDelayed()
        }
        alibControlsListener?.onPlaybackStateChanged(isPlaying)
    }

    override fun show() {
        super.show()
        if (videoView?.isPlaying == false) {
            hideDelayed()
        }
    }

    override fun registerListeners() {
        super.registerListeners()
        controlMinusOpening.setOnClickListener { alibControlsListener?.onMinusClick() }
        controlPlusOpening.setOnClickListener { alibControlsListener?.onPlusClick() }
        controlsFullscreen.setOnClickListener { alibControlsListener?.onFullScreenClick() }
        controlsSettings.setOnClickListener { alibControlsListener?.onSettingsClick() }
    }

    override fun setTitle(title: CharSequence?) {
        toolbar.title = title
        updateTextContainerVisibility()
    }

    override fun setSubTitle(subTitle: CharSequence?) {
        toolbar.subtitle = subTitle
        updateTextContainerVisibility()
    }

    override fun setDescription(description: CharSequence?) {}

    override fun isTextContainerEmpty(): Boolean {
        return false
    }

    override fun animateVisibility(toVisible: Boolean) {

        Log.e("lalka", "animateVisibility $controlsEnabled, $toVisible, ${!controlsEnabled && toVisible}")
        if (!controlsEnabled && toVisible) {
            hide()
            return
        }
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
        loadingProgressBar.visible()

        if (initialLoad) {
            controlsContainer.gone()
            controlButtonsWrapper.gone()
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
        loadingProgressBar.gone()
        controlsContainer.visible()
        controlButtonsWrapper.visible()

        playPauseButton.isEnabled = true
        previousButton.isEnabled = enabledViews.get(com.devbrackets.android.exomedia.R.id.exomedia_controls_previous_btn, true)
        nextButton.isEnabled = enabledViews.get(com.devbrackets.android.exomedia.R.id.exomedia_controls_next_btn, true)

        updatePlaybackState(videoView != null && videoView!!.isPlaying)
    }

    fun setFullScreenMode(isFullscreen: Boolean) {
        val icRes = if (isFullscreen) {
            R.drawable.ic_arrow_collapse
        } else {
            R.drawable.ic_arrow_expand
        }
        controlsFullscreen.apply {
            setImageDrawable(context.getCompatDrawable(icRes))
        }
    }

    interface AlibControlsListener {
        fun onMinusClick()
        fun onPlusClick()
        fun onFullScreenClick()

        fun onBackClick()
        fun onSettingsClick()
        fun onPIPClick()

        fun onPlaybackStateChanged(isPlaying: Boolean)
    }
}