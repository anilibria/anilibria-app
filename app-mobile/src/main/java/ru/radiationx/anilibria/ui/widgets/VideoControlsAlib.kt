package ru.radiationx.anilibria.ui.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.view.MenuItem
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import com.devbrackets.android.exomedia.listener.VideoControlsSeekListener
import com.devbrackets.android.exomedia.ui.animation.BottomViewHideShowAnimation
import com.devbrackets.android.exomedia.ui.animation.TopViewHideShowAnimation
import com.devbrackets.android.exomedia.ui.widget.VideoControls
import com.devbrackets.android.exomedia.ui.widget.VideoControlsMobile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ViewVideoControlBinding
import ru.radiationx.anilibria.ui.widgets.gestures.VideoGestureEventsListener
import ru.radiationx.data.analytics.features.PlayerAnalytics
import ru.radiationx.data.entity.domain.release.PlayerSkips
import ru.radiationx.shared.ktx.EventFlow
import ru.radiationx.shared.ktx.android.getCompatDrawable
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
    private var playerAnalytics: PlayerAnalytics? = null

    private var playerSkips: PlayerSkips? = null
    private val skippedList = mutableSetOf<PlayerSkips.Skip>()

    private lateinit var binding: ViewVideoControlBinding

    init {
        setSeekListener(object : VideoControlsSeekListener {

            override fun onSeekStarted(): Boolean {
                return false
            }

            override fun onSeekEnded(seekTime: Long): Boolean {
                playerAnalytics?.rewindSeek(getSeekPercent(), seekTime)
                return false
            }
        })
    }

    fun setAnalytics(playerAnalytics: PlayerAnalytics) {
        this.playerAnalytics = playerAnalytics
    }

    fun setOpeningListener(listener: AlibControlsListener?) {
        alibControlsListener = listener
    }

    fun fitSystemWindows(fit: Boolean) {
        this.fitsSystemWindows = false
        binding.videoControlsRoot.fitsSystemWindows = fit
    }

    fun setPictureInPictureEnabled(enabled: Boolean) {
        pictureInPictureMenuItem?.isVisible = enabled
    }

    fun setSkips(skips: PlayerSkips?) {
        playerSkips = skips
        skippedList.clear()
    }

    /*override fun updateProgress(position: Long, duration: Long, bufferPercent: Int) {
        super.updateProgress(position, duration, bufferPercent)
        val percent = position.toFloat() / duration.toFloat()
        controlMinusOpening.isVisible = percent < 0.3
        controlPlusOpening.isVisible = percent < 0.3
    }*/

    private fun getSeekPercent(): Float {
        val player = videoView ?: return 0f
        if (player.duration <= 0) {
            return 0f
        }
        return player.currentPosition / player.duration.toFloat()
    }

    private fun getCurrentSkip(): PlayerSkips.Skip? {
        return playerSkips?.opening?.takeIf { checkSkip(it) }
            ?: playerSkips?.ending?.takeIf { checkSkip(it) }
    }

    private fun checkSkip(skip: PlayerSkips.Skip): Boolean {
        val position = videoView?.currentPosition ?: return false
        return !skippedList.contains(skip) && position >= skip.start && position <= skip.end
    }

    private fun cancelSkip() {
        getCurrentSkip()?.also { skippedList.add(it) }
    }


    override fun getLayoutResource() = R.layout.view_video_control

    override fun retrieveViews() {
        super.retrieveViews()
        val viewRoot = findViewById<CoordinatorLayout>(R.id.videoControlsRoot)
        binding = ViewVideoControlBinding.bind(viewRoot)
        textContainer = binding.appbarLayout

        binding.btSkipsCancel.setOnClickListener {
            cancelSkip()
        }
        binding.btSkipsSkip.setOnClickListener {
            getCurrentSkip()?.also {
                videoView?.seekTo(it.end)
            }
        }

        binding.appbarLayout.apply {
            background = context.getCompatDrawable(R.drawable.bg_video_toolbar)
        }

        binding.toolbar.apply {
            navigationIcon = context.getCompatDrawable(R.drawable.ic_toolbar_arrow_back)
            setNavigationOnClickListener {
                alibControlsListener?.onBackClick()
            }
            pictureInPictureMenuItem = menu.add("Картинка в картинке")
                .setIcon(context.getCompatDrawable(R.drawable.ic_picture_in_picture_alt_toolbar))
                .setOnMenuItemClickListener {
                    alibControlsListener?.onPIPClick()
                    true
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        controlsContainer = binding.timeControlsContainer
        binding.gesturesControllerView.setEventsListener(object : VideoGestureEventsListener {
            private var localSeekDelta = 0L
            private var swipeSeekStarted = false
            private var tapSeekStarted = false

            private var tapJob: Job? = null
            private val tapSeekHandler = Handler()
            private val tapSeekRunnable = Runnable {
                applyPlayerSeek()
                binding.gestureSeekValue.isGone = true
                tapJob?.cancel()
                tapSeekStarted = false
                localSeekDelta = 0
            }

            private val tapRelay = EventFlow<MotionEvent>()

            private fun handleEndSwipeSeek() {
                applyPlayerSeek()
                binding.gestureSeekValue.isGone = true
                binding.gesturesControllerView.background = null
                swipeSeekStarted = false
                localSeekDelta = 0
            }

            private fun handleStartTapSeek() {
                tapJob?.cancel()
                tapJob = tapRelay
                    .observe()
                    .onEach { handleTapSeek(it) }
                    .flowOn(Dispatchers.Main.immediate)
                    .launchIn(GlobalScope)
            }

            private fun handleTapSeek(event: MotionEvent?) {
                val xPos = (event?.x ?: 0f).toInt()
                val seconds = if (xPos > binding.gesturesControllerView.width / 2) 10L else -10L
                val seekMillis = TimeUnit.SECONDS.toMillis(seconds)

                localSeekDelta += seekMillis

                val textValue =
                    "${if (localSeekDelta > 0) "+" else "-"}${Date(localSeekDelta.absoluteValue).asTimeSecString()}"
                binding.gestureSeekValue.text = textValue
            }

            private fun handleEndTapSeek() {
                tapSeekHandler.removeCallbacks(tapSeekRunnable)
                tapSeekHandler.postDelayed(tapSeekRunnable, 500)
            }

            private fun applyPlayerSeek() {
                videoView?.apply {
                    if (localSeekDelta != 0L) {
                        seekTo((currentPosition + localSeekDelta).coerceIn(0, duration))
                    }
                }
            }

            override fun onTap(event: MotionEvent?) {
                event ?: return
                videoView?.showControls()
                if (tapSeekStarted) {
                    tapRelay.set(event)
                }
            }

            override fun onDoubleTap(event: MotionEvent?) {
                event ?: return
                if (!tapSeekStarted) {
                    binding.gestureSeekValue.isVisible = true
                    tapSeekStarted = true
                    handleStartTapSeek()
                }
                if (tapSeekStarted) {
                    tapRelay.set(event)
                }
            }

            override fun onHorizontalScroll(event: MotionEvent?, delta: Float) {
                if (!swipeSeekStarted) {
                    binding.gestureSeekValue.isVisible = true
                    swipeSeekStarted = true
                }

                val duration = videoView?.duration ?: 0
                val currentPosition = videoView?.currentPosition ?: 0
                val percent: Int = ((delta / binding.gesturesControllerView.width) * 100).toInt()
                val seconds =
                    (pow(percent.toDouble(), 2.0) / 25).toLong() * if (percent < 0) -1 else 1
                val seekMillis = TimeUnit.SECONDS.toMillis(seconds)
                val targetPosition = (currentPosition + seekMillis).coerceIn(0, duration)

                val textValue =
                    "${if (seekMillis > 0) "+" else "-"}${Date(seekMillis.absoluteValue).asTimeSecString()}"

                binding.gestureSeekValue.text = textValue
                localSeekDelta = seekMillis
            }

            override fun onVerticalScroll(event: MotionEvent?, delta: Float) {
            }

            override fun onSwipeRight() {
            }

            override fun onSwipeLeft() {
            }

            override fun onSwipeBottom() {
            }

            override fun onSwipeTop() {
            }

            override fun onStart() {
            }

            override fun onEnd() {
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
        binding.controlMinusOpening.setOnClickListener { alibControlsListener?.onMinusClick() }
        binding.controlPlusOpening.setOnClickListener { alibControlsListener?.onPlusClick() }
        binding.controlsFullscreen.setOnClickListener { alibControlsListener?.onFullScreenClick() }
        binding.controlsSettings.setOnClickListener { alibControlsListener?.onSettingsClick() }
    }

    override fun setTitle(title: CharSequence?) {
        binding.toolbar.title = title
        updateTextContainerVisibility()
    }

    override fun setSubTitle(subTitle: CharSequence?) {
        binding.toolbar.subtitle = subTitle
        updateTextContainerVisibility()
    }

    override fun setDescription(description: CharSequence?) {}

    override fun isTextContainerEmpty(): Boolean {
        return false
    }

    override fun animateVisibility(toVisible: Boolean) {
        if (!controlsEnabled && toVisible) {
            hide()
            return
        }
        if (isVisible == toVisible) {
            return
        }

        if (!hideEmptyTextContainer || !isTextContainerEmpty) {
            textContainer.startAnimation(
                TopViewHideShowAnimation(
                    textContainer,
                    toVisible,
                    VideoControls.CONTROL_VISIBILITY_ANIMATION_LENGTH
                )
            )
        }

        if (!isLoading) {
            controlsContainer.startAnimation(
                BottomViewHideShowAnimation(
                    controlsContainer,
                    toVisible,
                    VideoControls.CONTROL_VISIBILITY_ANIMATION_LENGTH
                )
            )
            binding.controlButtonsWrapper.startAnimation(
                CenterViewHideShowAnimation(
                    binding.controlButtonsWrapper,
                    toVisible,
                    225
                )
            )

            val colorDark = Color.argb(127, 0, 0, 0)
            val colorFrom = if (toVisible) Color.TRANSPARENT else colorDark
            val colorTo = if (!toVisible) Color.TRANSPARENT else colorDark
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
            colorAnimation.duration = 335 // milliseconds
            colorAnimation.addUpdateListener { animator ->
                binding.videoControlsRoot.setBackgroundColor(
                    animator.animatedValue as Int
                )
            }
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
        loadingProgressBar.isVisible = true

        if (initialLoad) {
            controlsContainer.isGone = true
            binding.controlButtonsWrapper.isGone = true
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
        loadingProgressBar.isGone = true
        controlsContainer.isVisible = true
        binding.controlButtonsWrapper.isVisible = true

        playPauseButton.isEnabled = true
        previousButton.isEnabled = enabledViews.get(
            com.devbrackets.android.exomedia.R.id.exomedia_controls_previous_btn,
            true
        )
        nextButton.isEnabled =
            enabledViews.get(com.devbrackets.android.exomedia.R.id.exomedia_controls_next_btn, true)

        updatePlaybackState(videoView != null && videoView!!.isPlaying)
    }

    override fun updateProgress(position: Long, duration: Long, bufferPercent: Int) {
        super.updateProgress(position, duration, bufferPercent)
        val skip = getCurrentSkip()
        binding.btSkipsSkip.isVisible = skip != null
        binding.btSkipsCancel.isVisible = skip != null
    }

    fun setFullScreenMode(isFullscreen: Boolean) {
        val icRes = if (isFullscreen) {
            R.drawable.ic_arrow_collapse
        } else {
            R.drawable.ic_arrow_expand
        }
        binding.controlsFullscreen.apply {
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