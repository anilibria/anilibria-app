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


class VideoControlsAlib(context: Context) : VideoControlsMobile(context) {

    private var openingButtonsListener: OpeningButtonsListener? = null

    fun setOpeningListener(listener: OpeningButtonsListener) {
        openingButtonsListener = listener
    }

    override fun getLayoutResource() = R.layout.view_video_control

    override fun retrieveViews() {
        super.retrieveViews()
        controlsContainer = timeControlsContainer
    }

    override fun registerListeners() {
        super.registerListeners()
        controlMinusOpening.setOnClickListener { openingButtonsListener?.onMinusClick() }
        controlPlusOpening.setOnClickListener { openingButtonsListener?.onPlusClick() }
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

    interface OpeningButtonsListener {
        fun onMinusClick()
        fun onPlusClick()
    }
}