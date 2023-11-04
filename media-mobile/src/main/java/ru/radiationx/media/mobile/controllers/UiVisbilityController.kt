package ru.radiationx.media.mobile.controllers

import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.radiationx.media.mobile.PlayerFlow
import ru.radiationx.media.mobile.databinding.ViewPlayerBinding
import ru.radiationx.media.mobile.holder.PlayerAttachListener
import ru.radiationx.media.mobile.holder.TransitionHelper
import ru.radiationx.media.mobile.models.PlaybackState

internal class UiVisbilityController(
    private val coroutineScope: CoroutineScope,
    private val playerFlow: PlayerFlow,
    private val binding: ViewPlayerBinding,
) : PlayerAttachListener {

    companion object {
        private const val CONTROLS_HIDE_DELAY = 2000L
    }

    private var tapJob: Job? = null

    private val _internalState = MutableStateFlow(InternalState())

    private val _state = MutableStateFlow(UiVisibilityState())
    val state = _state.asStateFlow()

    init {
        val targetViews = listOf(
            binding.mediaButtonsContainer,
            binding.mediaFooter,
            binding.mediaFooterContainer,
            binding.mediaLoading,
            binding.mediaSeekerTime,
            binding.mediaScrim,
            binding.mediaSkipContainer,
            binding.mediaErrorContainer,
            binding.mediaScaleStroke
        )
        val transitionHelper = TransitionHelper(binding.mediaOverlay, targetViews)

        combine(
            _internalState,
            playerFlow.playerState
        ) { internalState, playerState ->
            val hasError = playerState.errorMessage != null
            val seekerVisible =
                (internalState.scrollSeeker || internalState.doubleTapSeeker) && !internalState.liveScale
            val mainVisible =
                (internalState.main || internalState.slider) && !internalState.liveScale
            val hasPip = internalState.pip

            _state.value = UiVisibilityState(
                mainVisible = !hasPip && mainVisible,
                controlsVisible = !hasPip && !seekerVisible && mainVisible && !hasError,
                seekerVisible = !hasPip && seekerVisible,
                loadingVisible = playerState.isBlockingLoading,
                skipVisible = !hasPip && internalState.skip && !internalState.liveScale,
                errorVisible = !hasPip && !seekerVisible && hasError,
                errorInPipVisible = hasPip && hasError,
                liveScaleVisible = !hasPip && internalState.liveScale,
                needsTransition = !hasPip
            )
        }.launchIn(coroutineScope)

        _state.onEach {
            if (it.needsTransition) {
                transitionHelper.beginDelayedTransition()
            } else {
                transitionHelper.endTransition()
            }
            binding.mediaButtonsContainer.isVisible = it.controlsVisible
            binding.mediaFooter.isVisible = it.mainVisible
            binding.mediaLoading.isVisible = it.loadingVisible
            binding.mediaSeekerTime.isVisible = it.seekerVisible
            binding.mediaScrim.isVisible = it.mainVisible
            binding.mediaSkipContainer.isVisible = it.skipVisible
            binding.mediaErrorContainer.isVisible = it.errorVisible
            binding.mediaErrorInPip.isVisible = it.errorInPipVisible
            binding.mediaScaleStroke.isVisible = it.liveScaleVisible
        }.launchIn(coroutineScope)
    }

    fun showMain() {
        startDelayedHideControls()
    }

    fun toggleMainVisible() {
        tapJob?.cancel()
        if (_internalState.value.main) {
            setMainState(false)
            return
        }
        startDelayedHideControls()
    }

    fun updateDoubleTapSeeker(active: Boolean) {
        _internalState.update { it.copy(doubleTapSeeker = active) }
    }

    fun updateScrollSeeker(active: Boolean) {
        _internalState.update { it.copy(scrollSeeker = active) }
    }

    fun updateSlider(active: Boolean) {
        _internalState.update { it.copy(slider = active) }
    }

    fun updateSkip(active: Boolean) {
        _internalState.update { it.copy(skip = active) }
    }

    fun updateLiveScale(active: Boolean) {
        _internalState.update { it.copy(liveScale = active) }
    }

    fun updatePip(active: Boolean) {
        _internalState.update { it.copy(pip = active) }
    }

    private fun startDelayedHideControls() {
        tapJob?.cancel()
        setMainState(true)
        tapJob = coroutineScope.launch {
            val needsHide = playerFlow.playerState.value.let {
                it.playWhenReady && it.playbackState != PlaybackState.ENDED
            }
            delay(CONTROLS_HIDE_DELAY)
            if (needsHide) {
                setMainState(false)
            }
        }
    }

    private fun setMainState(state: Boolean) {
        _internalState.update { it.copy(main = state) }
    }

    private data class InternalState(
        val main: Boolean = false,
        val doubleTapSeeker: Boolean = false,
        val scrollSeeker: Boolean = false,
        val slider: Boolean = false,
        val skip: Boolean = false,
        val liveScale: Boolean = false,
        val pip: Boolean = false,
    )

    internal data class UiVisibilityState(
        val mainVisible: Boolean = false,
        val controlsVisible: Boolean = false,
        val seekerVisible: Boolean = false,
        val loadingVisible: Boolean = false,
        val skipVisible: Boolean = false,
        val errorVisible: Boolean = false,
        val errorInPipVisible: Boolean = false,
        val liveScaleVisible: Boolean = false,
        val needsTransition: Boolean = false,
    )
}
