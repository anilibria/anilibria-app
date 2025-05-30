package ru.radiationx.anilibria.ui.activities.player

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ActivityVideoplayerBinding
import ru.radiationx.anilibria.ui.activities.BaseActivity
import ru.radiationx.anilibria.ui.activities.player.controllers.KeepScreenOnController
import ru.radiationx.anilibria.ui.activities.player.controllers.OrientationController
import ru.radiationx.anilibria.ui.activities.player.controllers.PictureInPictureController
import ru.radiationx.anilibria.ui.activities.player.controllers.PlayerSettingsController
import ru.radiationx.anilibria.ui.activities.player.di.SharedPlayerData
import ru.radiationx.anilibria.ui.activities.player.ext.getEpisode
import ru.radiationx.anilibria.ui.activities.player.mappers.toPlaylistItem
import ru.radiationx.anilibria.ui.activities.player.models.PlayerAction
import ru.radiationx.anilibria.ui.activities.player.playlist.PlaylistDialogFragment
import ru.radiationx.data.analytics.features.ActivityLaunchAnalytics
import ru.radiationx.data.analytics.features.PlayerAnalytics
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.media.mobile.models.PlayButtonState
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import ru.radiationx.quill.installModules
import ru.radiationx.quill.quillModule
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.isLaunchedFromHistory
import ru.radiationx.shared.ktx.android.launchInStarted
import ru.radiationx.shared.ktx.android.startMainActivity

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class VideoPlayerActivity : BaseActivity(R.layout.activity_videoplayer) {

    companion object {
        private const val ARG_EPISODE_ID = "ARG_EPISODE_ID"
        private const val KEY_EPISODE_ID = "KEY_EPISODE_ID"

        fun newIntent(context: Context, episodeId: EpisodeId) =
            Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(ARG_EPISODE_ID, episodeId)
                R.attr.bgDonationIconDa
            }
    }


    private val playAction = PictureInPictureController.Action(
        code = 1,
        title = "Пуск",
        icRes = ru.radiationx.media.mobile.R.drawable.ic_media_play_arrow_24
    )

    private val pauseAction = PictureInPictureController.Action(
        code = 2,
        title = "Пауза",
        icRes = ru.radiationx.media.mobile.R.drawable.ic_media_pause_24
    )

    private val replayAction = PictureInPictureController.Action(
        code = 3,
        title = "Реплей",
        icRes = ru.radiationx.media.mobile.R.drawable.ic_media_replay_24
    )

    private val prevAction = PictureInPictureController.Action(
        code = 4,
        title = "Предыдущая",
        icRes = ru.radiationx.media.mobile.R.drawable.ic_media_skip_previous_24
    )

    private val nextAction = PictureInPictureController.Action(
        code = 5,
        title = "Слудующая",
        icRes = ru.radiationx.media.mobile.R.drawable.ic_media_skip_next_24
    )

    private val pipController by lazy { PictureInPictureController(this) }

    private val orientationController by lazy { OrientationController(this) }

    private val keepScreenOnController by lazy { KeepScreenOnController(this) }

    private val settingsController by lazy { PlayerSettingsController(this) }

    private val binding by viewBinding<ActivityVideoplayerBinding>()

    private val analyticsListener by inject<PlayerAnalyticsListener>()

    private val analytics by inject<PlayerAnalytics>()

    private val player by inject<PlayerHolder>()

    private val viewModel by viewModel<PlayerViewModel>()

    private val settingsViewModel by viewModel<PlayerSettingViewModel>()

    override fun attachBaseContext(newBase: Context?) {
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
        super.attachBaseContext(newBase)
    }

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (isLaunchedFromHistory()) {
            get<ActivityLaunchAnalytics>().launchFromHistory(this, savedInstanceState)
            startMainActivity()
            finish()
            return
        }
        installModules(quillModule {
            instance { SharedPlayerData(getExtraNotNull(ARG_EPISODE_ID)) }
        })
        initKeepScreenOnController()
        initUiController()
        initFullscreenController()
        initPipController()
        initSettingsController()


        viewModel.actions.onEach { action ->
            when (action) {
                is PlayerAction.PlayEpisode -> {
                    val items = action.episodes.map { it.toPlaylistItem() }
                    val index = action.episodes.indexOfFirst { it.id == action.episodeId }
                    binding.playerView.prepare(items, index, action.seek)
                }

                is PlayerAction.PlaylistChange -> {
                    val items = action.episodes.map { it.toPlaylistItem() }
                    binding.playerView.changePlaylist(items)
                }

                is PlayerAction.Play -> {
                    if (action.seek != null && action.seek != binding.playerView.timelineState.value.position) {
                        binding.playerView.seekTo(action.seek)
                    }
                    // fix case when app hidden before "play" called
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        binding.playerView.play()
                    }
                }

                is PlayerAction.ShowPlaylist -> {
                    PlaylistDialogFragment().show(supportFragmentManager, "playlist")
                }
            }
        }.launchIn(lifecycleScope)

        viewModel.loadingState.onEach {
            binding.playerToolbarTitle.text = it.data?.title
            binding.playerToolbarSubtitle.text = it.data?.episodeTitle

            binding.dataLoadingContainer.isVisible = it.loading || it.error != null
            binding.dataLoading.isVisible = it.loading
            binding.dataErrorMessage.isVisible = it.error != null
            binding.dataErrorTitle.isVisible = it.error != null
            binding.dataErrorAction.isVisible = it.error != null
            binding.dataErrorMessage.text = it.error?.message
        }.launchIn(lifecycleScope)

        settingsViewModel.actions.onEach {
            settingsController.show()
        }.launchIn(lifecycleScope)

        settingsViewModel.settingsState.onEach {
            settingsController.setState(it)
            binding.playerView.setSpeed(it.currentSpeed)
            binding.playerView.setSkipsEnabled(it.skipsEnabled)
            binding.playerView.setSkipsTimerEnabled(it.skipsTimerEnabled)
        }.launchIn(lifecycleScope)

        settingsViewModel.settingsState.onEach {
            player.getPlayer().pauseAtEndOfMediaItems = !it.autoplayEnabled
        }.launchInStarted(this)

        binding.playerView.timelineState
            .sample(10000)
            .filter { it.duration > 0 }
            .map { it.position }
            .distinctUntilChanged()
            .onEach { position ->
                val episode = binding.playerView.playlistState.value.currentItem?.getEpisode()
                if (episode != null) {
                    viewModel.saveEpisodeSeek(episode.id, position)
                }
            }
            .launchIn(lifecycleScope)

        binding.playerView.mediaItemTransitionFlow
            .onEach { binding.playerView.pause() }
            .onEach { transition ->
                val episode = transition.mediaItem?.getEpisode() ?: return@onEach
                viewModel.onEpisodeChanged(episode.id)
            }
            .mapLatest { transition ->
                val timeline = binding.playerView.timelineState.first { it.duration > 0 }
                transition to timeline
            }
            .onEach { (transition, timeline) ->
                val episode = transition.mediaItem?.getEpisode() ?: return@onEach
                viewModel.onEpisodeTransition(episode.id, timeline.duration)
            }
            .launchIn(lifecycleScope)

        handleEpisode(intent, savedInstanceState, false)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleEpisode(intent, null, true)
    }

    override fun onBackPressed() {
        if (!binding.playerView.uiLockState.value) {
            super.onBackPressed()
        }
    }

    @UnstableApi
    override fun onStart() {
        super.onStart()
        analytics.screenStart()

        player.attach(this)
        analyticsListener.transport = player.selectedTransport
        player.getPlayer().addAnalyticsListener(analyticsListener)
        binding.playerView.setPlayer(player.getPlayer())
    }

    override fun onStop() {
        super.onStop()
        analytics.screenStop()

        binding.playerView.pause()
        val timeline = binding.playerView.timelineState.value.takeIf { it.duration > 0 }
        val episode = binding.playerView.playlistState.value.currentItem?.getEpisode()
        if (timeline != null && episode != null) {
            viewModel.saveEpisodeSeek(episode.id, timeline.position)
        }

        player.getPlayer().removeAnalyticsListener(analyticsListener)
        binding.playerView.setPlayer(null)
        player.detach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_EPISODE_ID, viewModel.episodeId.value)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isLaunchedFromHistory()) {
            binding.playerView.destroy()
            player.destroy()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        keepScreenOnController.onUserInteraction()
        binding.playerView.onInteraction()
    }

    private fun handleEpisode(intent: Intent, bundle: Bundle?, isNewIntent: Boolean) {
        val intentEpisodeId = intent.getExtraNotNull<EpisodeId>(ARG_EPISODE_ID)
        val savedEpisodeId = bundle?.getExtra<EpisodeId>(KEY_EPISODE_ID)
        val episodeId = if (isNewIntent) {
            intentEpisodeId
        } else {
            savedEpisodeId ?: intentEpisodeId
        }
        analytics.handleEpisode(
            isNewIntent = isNewIntent,
            hasBundle = bundle != null,
            episodeId = episodeId
        )
        viewModel.initialPlayEpisode(episodeId)
    }

    private fun initSettingsController() {
        settingsController.onQualitySelected = {
            settingsViewModel.onQualitySelected(it)
        }

        settingsController.onSpeedSelected = {
            settingsViewModel.onSpeedSelected(it)
        }

        settingsController.onSkipsSelected = {
            settingsViewModel.onSkipsEnabledSelected(it)
        }

        settingsController.onSkipsTimerSelected = {
            settingsViewModel.onSkipsTimerEnabledChange(it)
        }

        settingsController.onInactiveTimerSelected = {
            settingsViewModel.onInactiveTimerEnabledChange(it)
        }

        settingsController.onAutoplaySelected = {
            settingsViewModel.onAutoplayEnabledChange(it)
        }

        binding.playerView.onSkipClick = {
            settingsViewModel.onSkipsTimerEnabledChange(true)
        }

        binding.playerView.onCancelSkipClick = {
            settingsViewModel.onSkipsTimerEnabledChange(false)
        }
    }

    private fun initUiController() {
        binding.playerToolbarBack.setOnClickListener {
            finish()
        }

        binding.dataErrorAction.setOnClickListener {
            viewModel.refresh()
        }

        binding.playerView.onSettingsClick = {
            settingsViewModel.onSettingsClick()
        }

        binding.playerToolbarPlaylist.setOnClickListener {
            viewModel.onPlaylistClick()
        }

        val transition = AutoTransition().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            duration = 200
            addTarget(binding.playerToolbar)
        }
        combine(
            viewModel.loadingState.map { it.loading || it.error != null }.distinctUntilChanged(),
            binding.playerView.uiShowState
        ) { loadingVisible, playerVisible ->
            val isUiVisible = loadingVisible || playerVisible
            TransitionManager.beginDelayedTransition(binding.root, transition)
            binding.playerToolbar.isVisible = isUiVisible
            WindowCompat.getInsetsController(window, binding.root).apply {
                if (isUiVisible) {
                    show(WindowInsetsCompat.Type.navigationBars())
                    show(WindowInsetsCompat.Type.statusBars())
                    show(WindowInsetsCompat.Type.displayCutout())
                } else {
                    hide(WindowInsetsCompat.Type.navigationBars())
                    hide(WindowInsetsCompat.Type.statusBars())
                    hide(WindowInsetsCompat.Type.displayCutout())
                }
            }
        }
            .launchIn(lifecycleScope)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val barInsets = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val toolbarInsets = Insets.max(barInsets, cutoutInsets)
            binding.playerToolbar.updatePadding(
                left = toolbarInsets.left,
                top = toolbarInsets.top,
                right = toolbarInsets.right,
                bottom = toolbarInsets.bottom
            )
            binding.playerToolbarTitleContainer.updatePadding(
                left = toolbarInsets.right,
                right = toolbarInsets.left
            )
            insets
        }
    }

    private fun initKeepScreenOnController() {
        binding.playerView.playerState.onEach {
            keepScreenOnController.setPlaying(it.isPlaying)
        }.launchIn(lifecycleScope)

        keepScreenOnController.state.onEach {
            binding.root.keepScreenOn = it
        }.launchIn(lifecycleScope)

        settingsViewModel.settingsState.onEach {
            keepScreenOnController.setTimerEnabled(it.inactiveTimerEnabled)
        }.launchIn(lifecycleScope)
    }

    private fun initFullscreenController() {
        WindowCompat.getInsetsController(window, binding.root).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = if (isInMultiWindowMode) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        orientationController.init()
        orientationController.setOrientation(OrientationController.Orientation.LANDSCAPE)
        orientationController.state.onEach {
            binding.playerView.setFullscreenVisible(it.available)
            binding.playerView.setFullscreenActive(it.actualOrientation == OrientationController.Orientation.LANDSCAPE)
        }.launchIn(lifecycleScope)

        binding.playerView.onFullscreenClick = {
            orientationController.updateOrientation {
                if (it == OrientationController.Orientation.LANDSCAPE) {
                    null
                } else {
                    OrientationController.Orientation.LANDSCAPE
                }
            }
        }

        binding.playerView.uiLockState.onEach {
            orientationController.setUiLock(it)
        }.launchIn(lifecycleScope)
    }

    private fun initPipController() {
        pipController.init()
        binding.playerView.outputState.onEach { videoOutputState ->
            val aspectRatio = videoOutputState.videoSize.let {
                Rational(it.width, it.height)
            }
            pipController.updateParams {
                it.copy(
                    sourceHintRect = videoOutputState.hintRect,
                    aspectRatio = aspectRatio
                )
            }
        }.launchIn(lifecycleScope)

        combine(
            binding.playerView.playerState.map { it.commands }.distinctUntilChanged(),
            binding.playerView.playButtonState
        ) { commands, playButtonState ->
            buildList {
                add(prevAction.copy(isEnabled = commands.seekToPreviousMediaItem))
                when (playButtonState) {
                    PlayButtonState.PLAY -> add(playAction)
                    PlayButtonState.PAUSE -> add(pauseAction)
                    PlayButtonState.REPLAY -> add(replayAction)
                }
                add(nextAction.copy(isEnabled = commands.seekToNextMediaItem))
            }
        }.onEach { actions ->
            pipController.updateParams {
                it.copy(actions = actions)
            }
        }.launchIn(lifecycleScope)

        pipController.state.onEach {
            binding.playerView.setPipVisible(it.canEnter)
            binding.playerView.setPipActive(it.active)
        }.launchIn(lifecycleScope)

        binding.playerView.onPipClick = {
            analytics.pip()
            pipController.enter()
        }

        pipController.actionsListener = {
            when (it.code) {
                prevAction.code -> {
                    binding.playerView.prev()
                }

                nextAction.code -> {
                    binding.playerView.next()
                }

                playAction.code, pauseAction.code, replayAction.code -> {
                    binding.playerView.handlePlayClick()
                }
            }
        }
    }
}