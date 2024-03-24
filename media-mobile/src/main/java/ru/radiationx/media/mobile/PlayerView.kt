package ru.radiationx.media.mobile

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.media3.common.Player
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.radiationx.media.mobile.controllers.ErrorController
import ru.radiationx.media.mobile.controllers.LockController
import ru.radiationx.media.mobile.controllers.MediaActionsController
import ru.radiationx.media.mobile.controllers.MediaButtonsController
import ru.radiationx.media.mobile.controllers.OutputController
import ru.radiationx.media.mobile.controllers.SkipsController
import ru.radiationx.media.mobile.controllers.SpeedController
import ru.radiationx.media.mobile.controllers.TimelineController
import ru.radiationx.media.mobile.controllers.UiVisbilityController
import ru.radiationx.media.mobile.controllers.gesture.GestureController
import ru.radiationx.media.mobile.databinding.ViewPlayerBinding
import ru.radiationx.media.mobile.holder.RootPlayerHolder
import ru.radiationx.media.mobile.models.PlaylistItem

class PlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding by viewBinding<ViewPlayerBinding>(attachToRoot = true)

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val playerFlow = PlayerFlow(coroutineScope)
    private val holder = RootPlayerHolder()

    private val outputController = OutputController(
        coroutineScope = coroutineScope,
        playerFlow = playerFlow,
        mediaTextureView = binding.mediaTextureView,
        mediaAspectRatio = binding.mediaAspectRatio,
        scaleContainer = binding.mediaScaleContainer,
    )

    private val uiVisbilityController = UiVisbilityController(
        coroutineScope = coroutineScope,
        playerFlow = playerFlow,
        binding = binding
    )

    private val mediaButtonsController = MediaButtonsController(
        coroutineScope = coroutineScope,
        playerFlow = playerFlow,
        mediaButtonPrev = binding.mediaButtonPrev,
        mediaButtonPlay = binding.mediaButtonPlay,
        mediaButtonNext = binding.mediaButtonNext
    )

    private val mediaActionsController = MediaActionsController(
        coroutineScope = coroutineScope,
        playerFlow = playerFlow,
        mediaActionLock = binding.mediaActionLock,
        mediaActionPip = binding.mediaActionPip,
        mediaActionScale = binding.mediaActionScale,
        mediaActionSettings = binding.mediaActionSettings,
        mediaActionFullscreen = binding.mediaActionFullscreen,
    )

    private val lockController = LockController(
        coroutineScope = coroutineScope,
        playerFlow = playerFlow,
        container = binding.mediaLockContainer,
        button = binding.mediaButtonUnlock
    )

    private val timelineController = TimelineController(
        coroutineScope = coroutineScope,
        playerFlow = playerFlow,
        mediaSeekBar = binding.mediaSeekBar,
        mediaTime = binding.mediaTime
    )

    private val gestureController = GestureController(
        playerFlow = playerFlow,
        coroutineScope = coroutineScope,
        gestureView = binding.mediaGestures,
        seekerTime = binding.mediaSeekerTime,
        mediaAspectRatio = binding.mediaAspectRatio
    )

    private val skipsController = SkipsController(
        coroutineScope = coroutineScope,
        playerFlow = playerFlow,
        skipButtonCancel = binding.mediaSkipButtonCancel,
        skipButtonSkip = binding.mediaSkipButtonSkip
    )

    private val errorController = ErrorController(
        coroutineScope = coroutineScope,
        playerFlow = playerFlow,
        errorTitleText = binding.mediaErrorTitle,
        errorMessageText = binding.mediaErrorMessage,
        errorButtonAction = binding.mediaErrorAction
    )

    private val speedController = SpeedController(
        coroutineScope = coroutineScope,
        playerFlow = playerFlow
    )

    private val _uiShowState = MutableStateFlow(false)
    val uiShowState = _uiShowState.asStateFlow()

    private val _uiLockState = MutableStateFlow(false)
    val uiLockState = _uiLockState.asStateFlow()

    val outputState = outputController.outputState
    val playerState = playerFlow.playerState
    val playlistState = playerFlow.playlistState
    val timelineState = playerFlow.timelineState
    val playButtonState = mediaButtonsController.playButtonState
    val mediaItemTransitionFlow = playerFlow.mediaItemTransitionFlow

    var onPipClick: (() -> Unit)? = null
    var onSettingsClick: (() -> Unit)? = null
    var onFullscreenClick: (() -> Unit)? = null

    init {
        attachControllers()
        initUi()
        initOutput()
        initTimeLine()
        initGestures()
        initSkips()
        initMediaActions()
        initMediaButtons()
        initInsets()
    }

    fun setPlayer(player: Player?) {
        holder.setPlayer(player)
    }

    fun prepare(
        playlist: List<PlaylistItem>,
        startIndex: Int? = null,
        startPosition: Long? = null,
    ) {
        playerFlow.prepare(playlist, startIndex, startPosition)
    }

    fun changePlaylist(playlist: List<PlaylistItem>) {
        playerFlow.changePlaylist(playlist)
    }

    fun play() {
        playerFlow.play()
    }

    fun pause() {
        playerFlow.pause()
    }

    fun prev() {
        playerFlow.prev()
    }

    fun next() {
        playerFlow.next()
    }

    fun handlePlayClick() {
        mediaButtonsController.handlePlayClick()
    }

    fun seekTo(seek: Long) {
        playerFlow.seekTo(seek)
    }

    fun setSpeed(speed: Float) {
        speedController.setSpeed(speed)
    }

    fun setPipVisible(state: Boolean) {
        mediaActionsController.setPipVisible(state)
    }

    fun setPipActive(state: Boolean) {
        outputController.updatePip(state)
        uiVisbilityController.updatePip(state)
    }

    fun setFullscreenVisible(state: Boolean) {
        mediaActionsController.setFullscreenVisible(state)
    }

    fun setFullscreenActive(state: Boolean) {
        mediaActionsController.setFullscreenActive(state)
    }

    fun setSkipsEnabled(state: Boolean) {
        skipsController.setEnabled(state)
    }

    fun setSkipsTimerEnabled(state: Boolean) {
        skipsController.setTimerEnabled(state)
    }

    fun onInteraction() {
        if (uiLockState.value) {
            coroutineScope.launch {
                delay(300)
                lockController.onInteraction()
            }
        }
    }

    private fun attachControllers() {
        holder.addListener(playerFlow)
        holder.addListener(outputController)
        holder.addListener(uiVisbilityController)
        holder.addListener(mediaButtonsController)
        holder.addListener(timelineController)
        holder.addListener(gestureController)
        holder.addListener(skipsController)
        holder.addListener(errorController)
        holder.addListener(speedController)
        holder.addListener(lockController)
    }

    private fun initUi() {
        uiVisbilityController.state.onEach {
            _uiShowState.value = it.mainVisible
            _uiLockState.value = it.lockVisible
        }.launchIn(coroutineScope)

        lockController.onUnlockClick = {
            uiVisbilityController.updateLock(false)
            uiVisbilityController.showMain()
        }
    }

    private fun initMediaActions() {
        mediaActionsController.onAnyTap = {
            uiVisbilityController.showMain()
        }

        mediaActionsController.onLockClick = {
            uiVisbilityController.updateLock(true)
            lockController.onInteraction()
        }

        mediaActionsController.onPipClick = {
            onPipClick?.invoke()
        }

        mediaActionsController.onScaleClick = {
            outputController.toggleTarget()
        }

        mediaActionsController.onSettingsClick = {
            onSettingsClick?.invoke()
        }

        mediaActionsController.onFullscreenClick = {
            onFullscreenClick?.invoke()
        }
    }

    private fun initMediaButtons() {
        mediaButtonsController.onAnyTap = {
            uiVisbilityController.showMain()
        }
    }

    private fun initGestures() {
        gestureController.singleTapListener = {
            uiVisbilityController.toggleMainVisible()
        }

        gestureController.doubleTapSeekerState.onEach {
            uiVisbilityController.updateDoubleTapSeeker(it.isActive)
        }.launchIn(coroutineScope)

        gestureController.scrollSeekerState.onEach {
            uiVisbilityController.updateScrollSeeker(it.isActive)
        }.launchIn(coroutineScope)

        gestureController.longTapSeekerState.onEach {
            speedController.setLongTapSeek(it)
            uiVisbilityController.updateLongTapSeeker(it)
        }.launchIn(coroutineScope)

        gestureController.liveScale.onEach {
            outputController.setLiveScale(it)
            uiVisbilityController.updateLiveScale(it != null)
        }.launchIn(coroutineScope)
    }

    private fun initTimeLine() {
        timelineController.seekState.onEach {
            uiVisbilityController.updateSlider(it != null)
        }.launchIn(coroutineScope)
    }

    private fun initSkips() {
        skipsController.currentSkip.onEach {
            uiVisbilityController.updateSkip(it != null)
        }.launchIn(coroutineScope)
    }

    private fun initOutput() {
        outputController.state.onEach {
            mediaActionsController.setScaleVisible(it.canApply)
            mediaActionsController.setScaleType(it.target)
        }.launchIn(coroutineScope)
    }

    private fun initInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val barInsets = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val gesturesInsets = insets.getInsets(WindowInsetsCompat.Type.systemGestures())

            val footerInsets = Insets.max(barInsets, cutoutInsets)
            binding.mediaFooterContainer.updatePadding(
                left = footerInsets.left,
                top = footerInsets.top,
                right = footerInsets.right,
                bottom = footerInsets.bottom
            )

            binding.mediaLockContainer.updatePadding(
                top = footerInsets.top,
                bottom = footerInsets.bottom
            )

            binding.mediaGestures.updateLayoutParams<LayoutParams> {
                updateMargins(
                    left = gesturesInsets.left,
                    top = gesturesInsets.top,
                    right = gesturesInsets.right,
                    bottom = gesturesInsets.bottom
                )
            }

            insets
        }
    }
}