package ru.radiationx.anilibria.ui.activities

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Rational
import android.view.Surface
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.devbrackets.android.exomedia.core.video.scale.ScaleType
import com.devbrackets.android.exomedia.listener.OnCompletionListener
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.devbrackets.android.exomedia.listener.VideoControlsButtonListener
import com.devbrackets.android.exomedia.listener.VideoControlsVisibilityListener
import com.devbrackets.android.exomedia.ui.widget.VideoControlsCore
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.MediaSourceEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.michaelbel.bottomsheet.BottomSheet
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.anilibria.databinding.ActivityMyplayerBinding
import ru.radiationx.anilibria.extension.isDark
import ru.radiationx.anilibria.ui.widgets.VideoControlsAlib
import ru.radiationx.data.analytics.AnalyticsErrorReporter
import ru.radiationx.data.analytics.ErrorReporterConstants
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.PlayerAnalytics
import ru.radiationx.data.analytics.features.mapper.toAnalyticsPip
import ru.radiationx.data.analytics.features.mapper.toAnalyticsQuality
import ru.radiationx.data.analytics.features.mapper.toAnalyticsScale
import ru.radiationx.data.analytics.features.model.AnalyticsEpisodeFinishAction
import ru.radiationx.data.analytics.features.model.AnalyticsSeasonFinishAction
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.interactors.ReleaseInteractor
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.getColorFromAttr
import ru.radiationx.shared.ktx.android.immutableFlag
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

fun Int.toPrefQuality() = when (this) {
    MyPlayerActivity.VAL_QUALITY_SD -> PreferencesHolder.QUALITY_SD
    MyPlayerActivity.VAL_QUALITY_HD -> PreferencesHolder.QUALITY_HD
    MyPlayerActivity.VAL_QUALITY_FULL_HD -> PreferencesHolder.QUALITY_FULL_HD
    else -> PreferencesHolder.QUALITY_NO
}

class MyPlayerActivity : BaseActivity(R.layout.activity_myplayer) {

    companion object {
        const val ARG_RELEASE = "release"

        //const val ARG_CURRENT = "current"
        const val ARG_EPISODE_ID = "episode_id"
        const val ARG_QUALITY = "quality"
        const val ARG_PLAY_FLAG = "play_flag"

        const val VAL_QUALITY_SD = 0
        const val VAL_QUALITY_HD = 1
        const val VAL_QUALITY_FULL_HD = 2

        const val PLAY_FLAG_DEFAULT = 0
        const val PLAY_FLAG_FORCE_START = 1
        const val PLAY_FLAG_FORCE_CONTINUE = 2

        const val ACTION_REMOTE_CONTROL = "action.remote.control"
        const val EXTRA_REMOTE_CONTROL = "extra.remote.control"

        const val REMOTE_CONTROL_PLAY = 1
        const val REMOTE_CONTROL_PAUSE = 2
        const val REMOTE_CONTROL_PREV = 3
        const val REMOTE_CONTROL_NEXT = 4


        //private const val NOT_SELECTED = -1
        private const val NO_ID = -1

        private const val DEFAULT_QUALITY = VAL_QUALITY_SD
    }

    private lateinit var releaseData: Release
    private lateinit var currentEpisodeId: EpisodeId
    private var playFlag = PLAY_FLAG_DEFAULT
    private var currentQuality = DEFAULT_QUALITY

    //private var currentEpisode = NOT_SELECTED
    private var currentPlaySpeed = 1.0f
    private var videoControls: VideoControlsAlib? = null
    private val fullScreenListener = FullScreenListener()

    private val releaseInteractor by inject<ReleaseInteractor>()

    private val appThemeController by inject<AppThemeController>()

    private val defaultPreferences by inject<SharedPreferences>()

    private val appPreferences by inject<PreferencesHolder>()

    private val playerAnalytics by inject<PlayerAnalytics>()

    private val errorReporter by inject<AnalyticsErrorReporter>()

    private val binding by viewBinding<ActivityMyplayerBinding>()

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(playerAnalytics::useTime)
    }

    private val timeToStartCounter = TimeCounter()

    private val flagsHelper = PlayerWindowFlagHelper
    private var fullscreenOrientation = false

    private var currentFullscreen = false
    private var currentOrientation: Int = Configuration.ORIENTATION_UNDEFINED

    private val defaultScale = ScaleType.FIT_CENTER
    private var currentScale = defaultScale
    private var scaleEnabled = true

    private var currentPipControl = PreferencesHolder.PIP_BUTTON

    private val dialogController = SettingDialogController()

    private var pictureInPictureParams: PictureInPictureParams.Builder? = null

    private var releaseDataJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentData(intent)
        lifecycle.addObserver(useTimeCounter)
        timeToStartCounter.start()
        createPIPParams()
        initUiFlags()
        currentOrientation = resources.configuration.orientation
        goFullscreen()
        currentPlaySpeed = loadPlaySpeed()
        currentPipControl = loadPIPControl()

        binding.player.setScaleType(currentScale)
        binding.player.playbackSpeed = currentPlaySpeed
        binding.player.setOnPreparedListener(playerListener)
        binding.player.setOnCompletionListener(playerListener)
        binding.player.setOnVideoSizedChangedListener { intrinsicWidth, intrinsicHeight, pixelWidthHeightRatio ->
            updatePIPRatio(intrinsicWidth, intrinsicHeight)
        }
        binding.player.setAnalyticsListener(object : AnalyticsListener {

            private var wasFirstFrame = false
            private var lastLoadedUri: Uri? = null


            private var lastLoadError: Throwable? = null
            private var lastPlayerError: Throwable? = null


            override fun onRenderedFirstFrame(
                eventTime: AnalyticsListener.EventTime,
                surface: Surface?,
            ) {
                super.onRenderedFirstFrame(eventTime, surface)
                if (!wasFirstFrame) {
                    playerAnalytics.timeToStart(
                        lastLoadedUri?.host.toString(),
                        currentQuality.toPrefQuality().toAnalyticsQuality(),
                        timeToStartCounter.elapsed()
                    )
                    wasFirstFrame = true
                }
            }

            override fun onLoadError(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: MediaSourceEventListener.LoadEventInfo,
                mediaLoadData: MediaSourceEventListener.MediaLoadData,
                error: IOException,
                wasCanceled: Boolean,
            ) {
                super.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
                if (lastLoadError?.toString() != error.toString()) {
                    errorReporter.report(ErrorReporterConstants.group_player, "onLoadError", error)
                    playerAnalytics.error(error)
                    lastLoadError = error
                }
            }

            override fun onPlayerError(
                eventTime: AnalyticsListener.EventTime,
                error: ExoPlaybackException,
            ) {
                super.onPlayerError(eventTime, error)
                if (lastPlayerError?.toString() != error.toString()) {
                    errorReporter.report(
                        ErrorReporterConstants.group_player,
                        "onPlayerError",
                        error
                    )
                    playerAnalytics.error(error)
                    lastLoadError = error
                }
            }
        })


        videoControls = VideoControlsAlib(ContextThemeWrapper(this, this.theme), null, 0)

        videoControls?.apply {
            setAnalytics(playerAnalytics)
            updatePIPControl()
            binding.player.setControls(this as VideoControlsCore)
            setOpeningListener(alibControlListener)
            setVisibilityListener(ControlsVisibilityListener())
            let {
                it.setNextButtonRemoved(false)
                it.setPreviousButtonRemoved(false)
                it.setButtonListener(controlsListener)
            }
        }
        handleIntent(intent)
        updateUiFlags()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.also { handleIntent(it) }
    }

    override fun onUserLeaveHint() {
        if (checkPipMode() && currentPipControl == PreferencesHolder.PIP_AUTO) {
            enterPipMode()
        }
    }

    override fun onPause() {
        super.onPause()
        saveEpisodeAtNoZero()
    }

    override fun onStop() {
        super.onStop()
        binding.player.pause()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        newConfig.also { config ->
            updateByConfig(config)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.player.setOnPreparedListener(null)
        binding.player.setOnCompletionListener(null)
        binding.player.setOnVideoSizedChangedListener(null)
        binding.player.setAnalyticsListener(null)
        try {
            unregisterReceiver(mReceiver)
        } catch (ignore: Exception) {
        }

        videoControls?.apply {
            setOpeningListener(null)
            setVisibilityListener(null)
            setButtonListener(null)
        }
        saveEpisodeAtNoZero()
        binding.player.stopPlayback()
        exitFullscreen()
        videoControls = null
    }

    private fun hasPipPermission(): Boolean {
        val appOps = getSystemService<AppOpsManager>() ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        }
        val op = AppOpsManager.OPSTR_PICTURE_IN_PICTURE
        val pid = android.os.Process.myUid()
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(op, pid, packageName)
        } else {
            appOps.checkOpNoThrow(op, pid, packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun checkPipMode(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }
        val hasFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        val hasPermission = hasPipPermission()
        return hasFeature && hasPermission
    }

    private fun checkSausage(): Boolean {
        val size = windowManager.defaultDisplay.let {
            val size = Point()
            it.getRealSize(size)
            size
        }
        val notSausage = 16f / 9f

        val width = max(size.x, size.y)
        val height = min(size.x, size.y)
        val ratio = width.toFloat() / height.toFloat()

        return notSausage != ratio
    }

    private fun handleIntentData(intent: Intent) {
        val release = requireNotNull(intent.getParcelableExtra<Release>(ARG_RELEASE)) {
            "Release must be not null"
        }
        val episodeId = requireNotNull(intent.getParcelableExtra<EpisodeId>(ARG_EPISODE_ID)) {
            "Episode id must be not null"
        }
        val quality = intent.getIntExtra(ARG_QUALITY, DEFAULT_QUALITY)
        val playFlag = intent.getIntExtra(ARG_PLAY_FLAG, PLAY_FLAG_DEFAULT)

        this.releaseData = release
        this.currentEpisodeId = episodeId
        this.currentQuality = quality
        this.playFlag = playFlag

        releaseDataJob?.cancel()
        releaseDataJob = releaseInteractor.observeFull(releaseData.id, releaseData.code).onEach {
            releaseData = it
        }.launchIn(lifecycleScope)
    }

    private fun handleIntent(intent: Intent) {
        handleIntentData(intent)
        updateAndPlayRelease()
    }

    private fun updateAndPlayRelease() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(ActivityManager.TaskDescription(releaseData.title))
        }

        videoControls?.apply {
            setTitle(releaseData.title)
        }
        playEpisode(getEpisode())
    }

    private fun loadScale(orientation: Int): ScaleType {
        val scaleOrdinal =
            defaultPreferences.getInt("video_ratio_$orientation", defaultScale.ordinal)
        return ScaleType.fromOrdinal(scaleOrdinal)
    }

    private fun saveScale(orientation: Int, scale: ScaleType) {
        defaultPreferences.edit().putInt("video_ratio_$orientation", scale.ordinal).apply()
    }

    private fun savePlaySpeed() {
        playerAnalytics.settingsSpeedChange(currentPlaySpeed)
        releaseInteractor.setPlaySpeed(currentPlaySpeed)
    }

    private fun loadPlaySpeed(): Float {
        return releaseInteractor.getPlaySpeed()
    }

    private fun savePIPControl() {
        releaseInteractor.setPIPControl(currentPipControl)
    }

    private fun loadPIPControl(): Int {
        return releaseInteractor.getPIPControl()
    }

    private fun updateScale(scale: ScaleType) {
        val inMultiWindow = getInMultiWindow()
        currentScale = scale
        scaleEnabled = !inMultiWindow
        if (!inMultiWindow) {
            saveScale(currentOrientation, currentScale)
        }
        binding.player.setScaleType(currentScale)
    }

    private fun updateQuality(newQuality: Int) {
        this.currentQuality = newQuality
        val prefQuality = newQuality.toPrefQuality()
        playerAnalytics.settingsQualityChange(prefQuality.toAnalyticsQuality())
        appPreferences.setQuality(prefQuality)
        saveEpisode()
        updateAndPlayRelease()
    }

    private fun updatePlaySpeed(newPlaySpeed: Float) {
        currentPlaySpeed = newPlaySpeed
        binding.player.playbackSpeed = currentPlaySpeed
        savePlaySpeed()
    }

    private fun updatePIPControl(newPipControl: Int = currentPipControl) {
        currentPipControl = newPipControl
        val pipCheck = checkPipMode() && newPipControl == PreferencesHolder.PIP_BUTTON
        videoControls?.setPictureInPictureEnabled(pipCheck)
        savePIPControl()
    }

    private fun getInMultiWindow(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isInMultiWindowMode
        } else {
            false
        }
    }

    private fun getInPIP(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isInPictureInPictureMode
        } else {
            false
        }
    }

    private fun updateByConfig(config: Configuration) {
        val correctOrientation = config.orientation
        fullscreenOrientation = when (correctOrientation) {
            Configuration.ORIENTATION_LANDSCAPE -> true
            else -> false
        }
        currentOrientation = correctOrientation
        updateUiFlags()
        videoControls?.setFullScreenMode(fullscreenOrientation)
    }

    private fun saveEpisodeAtNoZero(position: Long = binding.player.currentPosition) {
        if (position == 0L) {
            return
        }
        saveEpisode(position)
    }

    private fun saveEpisode(position: Long = binding.player.currentPosition) {
        if (position < 0) {
            return
        }
        val newAccess = getEpisode().access.copy(
            seek = position,
            lastAccess = System.currentTimeMillis(),
            isViewed = true
        )
        // todo TR-274 run in scope outside lifecycle
        runBlocking {
            releaseInteractor.putEpisode(newAccess)
        }
    }

    private fun getNextEpisode(): Episode? {
        val currentIndex = releaseData.episodes.indexOfFirst { it.id == currentEpisodeId }
        return releaseData.episodes.getOrNull(currentIndex - 1)
    }

    private fun getPrevEpisode(): Episode? {
        val currentIndex = releaseData.episodes.indexOfFirst { it.id == currentEpisodeId }
        return releaseData.episodes.getOrNull(currentIndex + 1)
    }

    private fun getEpisode(id: EpisodeId = currentEpisodeId) =
        releaseData.episodes.first { it.id == id }

    private fun getEpisodeId(episode: Episode) =
        releaseData.episodes.first { it == episode }.id

    private fun playEpisode(episode: Episode) {
        when (playFlag) {
            PLAY_FLAG_DEFAULT -> {
                hardPlayEpisode(episode)
                if (episode.access.seek > 0) {
                    hardPlayEpisode(episode)
                    val titles = arrayOf("К началу", "К последней позиции")
                    AlertDialog.Builder(this)
                        .setTitle("Перемотать")
                        .setItems(titles) { _, which ->
                            if (which == 1) {
                                binding.player.seekTo(episode.access.seek)
                            }
                        }
                        .show()
                }
            }

            PLAY_FLAG_FORCE_START -> {
                hardPlayEpisode(episode)
            }

            PLAY_FLAG_FORCE_CONTINUE -> {
                hardPlayEpisode(episode)
            }
        }
        playFlag = PLAY_FLAG_FORCE_CONTINUE
    }

    private fun hardPlayEpisode(episode: Episode) {
        videoControls?.setSubTitle(
            "${episode.title} [${
                dialogController.getQualityTitle(
                    currentQuality
                )
            }]"
        )
        currentEpisodeId = getEpisodeId(episode)
        val videoPath = when (currentQuality) {
            VAL_QUALITY_SD -> episode.urlSd
            VAL_QUALITY_HD -> episode.urlHd
            VAL_QUALITY_FULL_HD -> episode.urlFullHd
            else -> null
        }
        videoControls?.also {
            it.setSkips(episode.skips)
            it.setNextButtonRemoved(currentEpisodeId == releaseData.episodes.firstOrNull()?.id)
            it.setPreviousButtonRemoved(currentEpisodeId == releaseData.episodes.lastOrNull()?.id)
        }
        videoPath?.also {
            binding.player.setVideoPath(it)
        }
    }

    private fun goFullscreen() {
        currentFullscreen = true
        updateUiFlags()
    }

    private fun exitFullscreen() {
        currentFullscreen = false
        updateUiFlags()
    }

    private fun initUiFlags() {
        window.decorView.also {
            it.setOnSystemUiVisibilityChangeListener(fullScreenListener)
        }
    }

    private fun updateUiFlags() {
        val scale = loadScale(currentOrientation)
        val inMultiWindow = getInMultiWindow()

        updateScale(if (inMultiWindow) defaultScale else scale)

        window.decorView.also {
            it.systemUiVisibility = flagsHelper.getFlags(currentOrientation, currentFullscreen)
        }

        videoControls?.fitSystemWindows(inMultiWindow || currentOrientation != Configuration.ORIENTATION_LANDSCAPE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = if (inMultiWindow) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.attributes = window.attributes
        }

        updatePIPRect()
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null || intent.action != ACTION_REMOTE_CONTROL) {
                return
            }
            val remoteControl = intent.getIntExtra(EXTRA_REMOTE_CONTROL, 0)
            when (remoteControl) {
                REMOTE_CONTROL_PLAY -> controlsListener.onPlayPauseClicked()
                REMOTE_CONTROL_PAUSE -> controlsListener.onPlayPauseClicked()
                REMOTE_CONTROL_PREV -> controlsListener.onPreviousClicked()
                REMOTE_CONTROL_NEXT -> controlsListener.onNextClicked()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean, newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        saveEpisode()
        if (isInPictureInPictureMode) {
            // Starts receiving events from action items in PiP mode.
            registerReceiver(mReceiver, IntentFilter(ACTION_REMOTE_CONTROL))
            //videoControls?.setControlsEnabled(false)
            videoControls?.hide()
            videoControls?.isGone = true
            updateByConfig(newConfig)

        } else {
            // We are out of PiP mode. We can stop receiving events from it.
            try {
                unregisterReceiver(mReceiver)
            } catch (ignore: Throwable) {
            }

            // Show the video controls if the video is not playing
            /*if (!mMovieView.isPlaying) {
                mMovieView.showControls()
            }*/
            //videoControls?.setControlsEnabled(true)

            updateByConfig(newConfig)
            binding.player.showControls()
            videoControls?.visibility = View.VISIBLE

            //player.showControls()
        }
        //updateUiFlags()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createPIPParams() {
        if (checkPipMode()) {
            pictureInPictureParams = PictureInPictureParams.Builder()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun updatePIPRatio(width: Int, height: Int) {
        if (checkPipMode()) {
            pictureInPictureParams?.setAspectRatio(Rational(width, height))
            updatePIPRect()
        }
        updatePictureInPictureParams()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun updatePIPRect() {
        if (checkPipMode()) {
            binding.player?.findViewById<View>(com.devbrackets.android.exomedia.R.id.exomedia_video_view)
                ?.also {
                    val rect = Rect(0, 0, 0, 0)
                    it.getGlobalVisibleRect(rect)
                    pictureInPictureParams?.setSourceRectHint(rect)
                }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun updatePictureInPictureParams() {
        if (checkPipMode()) {
            val params = pictureInPictureParams ?: return
            val playState = binding.player?.isPlaying ?: return
            val actions = mutableListOf<RemoteAction>()
            val maxActions = maxNumPictureInPictureActions




            if (actions.size < maxActions) {
                val icRes =
                    if (playState) R.drawable.ic_pause_remote else R.drawable.ic_play_arrow_remote
                val actionCode = if (playState) REMOTE_CONTROL_PAUSE else REMOTE_CONTROL_PLAY
                val title = if (playState) "Пауза" else "Пуск"

                actions.add(
                    RemoteAction(
                        Icon.createWithResource(this@MyPlayerActivity, icRes),
                        title,
                        title,
                        PendingIntent.getBroadcast(
                            this@MyPlayerActivity,
                            actionCode,
                            Intent(ACTION_REMOTE_CONTROL).putExtra(
                                EXTRA_REMOTE_CONTROL,
                                actionCode
                            ),
                            immutableFlag()
                        )
                    )
                )
            }

            if (actions.size < maxActions) {
                val icRes = R.drawable.ic_skip_next_remote
                val actionCode = REMOTE_CONTROL_NEXT
                val title = "Следующая серия"

                actions.add(
                    RemoteAction(
                        Icon.createWithResource(this@MyPlayerActivity, icRes),
                        title,
                        title,
                        PendingIntent.getBroadcast(
                            this@MyPlayerActivity,
                            actionCode,
                            Intent(ACTION_REMOTE_CONTROL).putExtra(
                                EXTRA_REMOTE_CONTROL,
                                actionCode
                            ),
                            immutableFlag()
                        )
                    )
                )
            }

            if (actions.size < maxActions) {
                val icRes = R.drawable.ic_skip_previous_remote
                val actionCode = REMOTE_CONTROL_PREV
                val title = "Предыдущая серия"

                actions.add(
                    0, RemoteAction(
                        Icon.createWithResource(this@MyPlayerActivity, icRes),
                        title,
                        title,
                        PendingIntent.getBroadcast(
                            this@MyPlayerActivity,
                            actionCode,
                            Intent(ACTION_REMOTE_CONTROL).putExtra(
                                EXTRA_REMOTE_CONTROL,
                                actionCode
                            ),
                            immutableFlag()
                        )
                    )
                )
            }

            params.setActions(actions)

            setPictureInPictureParams(params.build())
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun enterPipMode() {
        if (checkPipMode()) {
            pictureInPictureParams?.also {
                playerAnalytics.pip(getSeekPercent())
                videoControls?.isGone = true
                enterPictureInPictureMode(it.build())
            }
        }
    }

    private inner class SettingDialogController {
        private val settingQuality = 0
        private val settingPlaySpeed = 1
        private val settingScale = 2
        private val settingPIP = 3

        private var openedDialogs = mutableListOf<BottomSheet>()

        private fun BottomSheet.register() = openedDialogs.add(this)

        fun getQualityTitle(quality: Int) = when (quality) {
            VAL_QUALITY_SD -> "480p"
            VAL_QUALITY_HD -> "720p"
            VAL_QUALITY_FULL_HD -> "1080p"
            else -> "Вероятнее всего 480p"
        }

        fun getPlaySpeedTitle(speed: Float) = if (speed == 1.0f) {
            "Обычная"
        } else {
            "${"$speed".trimEnd('0').trimEnd('.').trimEnd(',')}x"
        }

        fun getScaleTitle(scale: ScaleType) = when (scale) {
            ScaleType.FIT_CENTER -> "Оптимально"
            ScaleType.CENTER_CROP -> "Обрезать"
            ScaleType.FIT_XY -> "Растянуть"
            else -> "Одному лишь богу известно"
        }

        fun getPIPTitle(pipControl: Int) = when (pipControl) {
            PreferencesHolder.PIP_AUTO -> "При скрытии экрана"
            PreferencesHolder.PIP_BUTTON -> "По кнопке"
            else -> "Одному лишь богу известно"
        }

        fun updateSettingsDialog() {
            if (openedDialogs.isNotEmpty()) {
                openedDialogs.forEach {
                    it.dismiss()
                }
                openedDialogs.clear()
                showSettingsDialog()
            }
        }

        fun showSettingsDialog() {
            if (openedDialogs.isNotEmpty()) {
                updateSettingsDialog()
                return
            }

            val qualityValue = getQualityTitle(currentQuality)
            val speedValue = getPlaySpeedTitle(currentPlaySpeed)
            val scaleValue = getScaleTitle(currentScale)
            val pipValue = getPIPTitle(currentPipControl)

            val valuesList = mutableListOf(
                settingQuality,
                settingPlaySpeed
            )
            if (checkSausage()) {
                valuesList.add(settingScale)
            }
            if (checkPipMode()) {
                valuesList.add(settingPIP)
            }

            val titles = valuesList
                .asSequence()
                .map {
                    when (it) {
                        settingQuality -> "Качество (<b>$qualityValue</b>)"
                        settingPlaySpeed -> "Скорость (<b>$speedValue</b>)"
                        settingScale -> "Соотношение сторон (<b>$scaleValue</b>)"
                        settingPIP -> "Режим окна (<b>$pipValue</b>)"
                        else -> "Привет"
                    }
                }
                .map { Html.fromHtml(it) }
                .toList()
                .toTypedArray()

            val icQualityRes = when (currentQuality) {
                VAL_QUALITY_SD -> R.drawable.ic_quality_sd_base
                VAL_QUALITY_HD -> R.drawable.ic_quality_hd_base
                VAL_QUALITY_FULL_HD -> R.drawable.ic_quality_full_hd_base
                else -> R.drawable.ic_settings
            }
            val icons = valuesList
                .asSequence()
                .map {
                    when (it) {
                        settingQuality -> icQualityRes
                        settingPlaySpeed -> R.drawable.ic_play_speed
                        settingScale -> R.drawable.ic_aspect_ratio
                        settingPIP -> R.drawable.ic_picture_in_picture_alt
                        else -> R.drawable.ic_anilibria
                    }
                }
                .map {
                    ContextCompat.getDrawable(this@MyPlayerActivity, it)
                }
                .toList()
                .toTypedArray()

            BottomSheet.Builder(this@MyPlayerActivity)
                .setItems(titles, icons) { _, which ->
                    when (valuesList[which]) {
                        settingQuality -> {
                            playerAnalytics.settingsQualityClick()
                            showQualityDialog()
                        }

                        settingPlaySpeed -> {
                            playerAnalytics.settingsSpeedClick()
                            showPlaySpeedDialog()
                        }

                        settingScale -> {
                            playerAnalytics.settingsScaleClick()
                            showScaleDialog()
                        }

                        settingPIP -> {
                            playerAnalytics.settingsPipClick()
                            showPIPDialog()
                        }
                    }
                }
                .setDarkTheme(appThemeController.getTheme().isDark())
                .setIconTintMode(PorterDuff.Mode.SRC_ATOP)
                .setIconColor(this@MyPlayerActivity.getColorFromAttr(R.attr.colorOnSurface))
                .setItemTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textDefault))
                .setBackgroundColor(this@MyPlayerActivity.getColorFromAttr(R.attr.colorSurface))
                .show()
                .register()
        }

        fun showPlaySpeedDialog() {
            val values = arrayOf(
                0.25f,
                0.5f,
                0.75f,
                1.0f,
                1.25f,
                1.5f,
                1.75f,
                2.0f
            )
            val activeIndex = values.indexOf(currentPlaySpeed)
            val titles = values
                .mapIndexed { index, s ->
                    val stringValue = getPlaySpeedTitle(s)
                    when (index) {
                        activeIndex -> "<b>$stringValue</b>"
                        else -> stringValue
                    }
                }
                .map { Html.fromHtml(it) }
                .toTypedArray()

            BottomSheet.Builder(this@MyPlayerActivity)
                .setTitle("Скорость воспроизведения")
                .setItems(titles) { _, which ->
                    updatePlaySpeed(values[which])
                }
                .setDarkTheme(appThemeController.getTheme().isDark())
                .setItemTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textDefault))
                .setTitleTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textSecond))
                .setBackgroundColor(this@MyPlayerActivity.getColorFromAttr(R.attr.colorSurface))
                .show()
                .register()
        }

        fun showQualityDialog() {
            val qualities = mutableListOf<Int>()
            if (getEpisode().urlSd != null) qualities.add(VAL_QUALITY_SD)
            if (getEpisode().urlHd != null) qualities.add(VAL_QUALITY_HD)
            if (getEpisode().urlFullHd != null) qualities.add(VAL_QUALITY_FULL_HD)

            val values = qualities.toTypedArray()

            val activeIndex = values.indexOf(currentQuality)
            val titles = values
                .mapIndexed { index, s ->
                    val stringValue = getQualityTitle(s)
                    if (index == activeIndex) "<b>$stringValue</b>" else stringValue
                }
                .map { Html.fromHtml(it) }
                .toTypedArray()

            BottomSheet.Builder(this@MyPlayerActivity)
                .setTitle("Качество")
                .setItems(titles) { _, which ->
                    updateQuality(values[which])
                }
                .setDarkTheme(appThemeController.getTheme().isDark())
                .setItemTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textDefault))
                .setTitleTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textSecond))
                .setBackgroundColor(this@MyPlayerActivity.getColorFromAttr(R.attr.colorSurface))
                .show()
                .register()
        }

        fun showScaleDialog() {
            val values = arrayOf(
                ScaleType.FIT_CENTER,
                ScaleType.CENTER_CROP,
                ScaleType.FIT_XY
            )
            val activeIndex = values.indexOf(currentScale)
            val titles = values
                .mapIndexed { index, s ->
                    val stringValue = getScaleTitle(s)
                    if (index == activeIndex) "<b>$stringValue</b>" else stringValue
                }
                .map { Html.fromHtml(it) }
                .toTypedArray()

            BottomSheet.Builder(this@MyPlayerActivity)
                .setTitle("Соотношение сторон")
                .setItems(titles) { _, which ->
                    val newScaleType = values[which]
                    playerAnalytics.settingsScaleChange(newScaleType.ordinal.toAnalyticsScale())
                    updateScale(newScaleType)
                }
                .setDarkTheme(appThemeController.getTheme().isDark())
                .setItemTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textDefault))
                .setTitleTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textSecond))
                .setBackgroundColor(this@MyPlayerActivity.getColorFromAttr(R.attr.colorSurface))
                .show()
                .register()
        }

        fun showPIPDialog() {
            val values = arrayOf(
                PreferencesHolder.PIP_AUTO,
                PreferencesHolder.PIP_BUTTON
            )
            val activeIndex = values.indexOf(currentPipControl)
            val titles = values
                .mapIndexed { index, s ->
                    val stringValue = getPIPTitle(s)
                    if (index == activeIndex) "<b>$stringValue</b>" else stringValue
                }
                .map { Html.fromHtml(it) }
                .toTypedArray()

            BottomSheet.Builder(this@MyPlayerActivity)
                .setTitle("Режим окна (картинка в картинке)")
                .setItems(titles) { _, which ->
                    val newPipControl = values[which]
                    playerAnalytics.settingsPipChange(newPipControl.toAnalyticsPip())
                    updatePIPControl(newPipControl)
                }
                .setDarkTheme(appThemeController.getTheme().isDark())
                .setItemTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textDefault))
                .setTitleTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textSecond))
                .setBackgroundColor(this@MyPlayerActivity.getColorFromAttr(R.attr.colorSurface))
                .show()
                .register()
        }
    }

    private fun showSeasonFinishDialog() {
        playerAnalytics.seasonFinish()
        val titles = arrayOf(
            "Начать серию заново",
            "Начать с первой серии",
            "Закрыть плеер"
        )
        BottomSheet.Builder(this@MyPlayerActivity)
            .setTitle("Серия полностью просмотрена")
            .setItems(titles) { _, which ->
                when (which) {
                    0 -> {
                        playerAnalytics.seasonFinishAction(AnalyticsSeasonFinishAction.RESTART_EPISODE)
                        saveEpisode(0)
                        hardPlayEpisode(getEpisode())
                    }

                    1 -> {
                        playerAnalytics.seasonFinishAction(AnalyticsSeasonFinishAction.RESTART_SEASON)
                        releaseData.episodes.lastOrNull()?.also {
                            hardPlayEpisode(it)
                        }
                    }

                    2 -> {
                        playerAnalytics.seasonFinishAction(AnalyticsSeasonFinishAction.CLOSE_PLAYER)
                        finish()
                    }
                }
            }
            .setDarkTheme(appThemeController.getTheme().isDark())
            .setItemTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textDefault))
            .setTitleTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textSecond))
            .setBackgroundColor(this@MyPlayerActivity.getColorFromAttr(R.attr.colorSurface))
            .show()
    }

    private fun showEpisodeFinishDialog() {
        playerAnalytics.episodesFinish()
        val titles = arrayOf(
            "Начать серию заново",
            "Включить следущую серию"
        )
        BottomSheet.Builder(this@MyPlayerActivity)
            .setTitle("Серия полностью просмотрена")
            .setItems(titles) { _, which ->
                when (which) {
                    0 -> {
                        playerAnalytics.episodesFinishAction(AnalyticsEpisodeFinishAction.RESTART)
                        saveEpisode(0)
                        hardPlayEpisode(getEpisode())
                    }

                    1 -> {
                        playerAnalytics.episodesFinishAction(AnalyticsEpisodeFinishAction.NEXT)
                        getNextEpisode()?.also { hardPlayEpisode(it) }
                    }
                }
            }
            .setDarkTheme(appThemeController.getTheme().isDark())
            .setItemTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textDefault))
            .setTitleTextColor(this@MyPlayerActivity.getColorFromAttr(R.attr.textSecond))
            .setBackgroundColor(this@MyPlayerActivity.getColorFromAttr(R.attr.colorSurface))
            .show()
    }

    private fun getSeekPercent(): Float {
        if (binding.player.duration <= 0) {
            return 0f
        }
        return binding.player.currentPosition / binding.player.duration.toFloat()
    }


    private val alibControlListener = object : VideoControlsAlib.AlibControlsListener {

        override fun onPIPClick() {
            enterPipMode()
        }

        override fun onBackClick() {
            finish()
        }

        override fun onSettingsClick() {
            playerAnalytics.settingsClick()
            dialogController.showSettingsDialog()
        }

        private val delta = TimeUnit.SECONDS.toMillis(90)
        override fun onMinusClick() {
            val newPosition = binding.player.currentPosition - delta
            binding.player.seekTo(newPosition.coerceIn(0, binding.player.duration))
        }

        override fun onPlusClick() {
            val newPosition = binding.player.currentPosition + delta
            binding.player.seekTo(newPosition.coerceIn(0, binding.player.duration))
        }

        override fun onFullScreenClick() {
            if (fullscreenOrientation) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            fullscreenOrientation = !fullscreenOrientation
            if (fullscreenOrientation) {
                playerAnalytics.fullScreen(getSeekPercent())
            }
            videoControls?.setFullScreenMode(fullscreenOrientation)
        }

        override fun onPlaybackStateChanged(isPlaying: Boolean) {
            updatePictureInPictureParams()
        }


    }

    private val playerListener = object : OnPreparedListener, OnCompletionListener {

        private fun startAndSeek(episode: Episode) {
            if (playFlag == PLAY_FLAG_FORCE_CONTINUE && episode.access.seek > 0) {
                binding.player.seekTo(episode.access.seek)
            }
            binding.player.start()
        }

        override fun onPrepared() {
            val episode = getEpisode()
            if (episode.access.seek >= binding.player.duration) {
                binding.player.pause()
                if (getNextEpisode() == null) {
                    showSeasonFinishDialog()
                } else {
                    showEpisodeFinishDialog()
                }
            } else {
                startAndSeek(episode)
            }
        }

        override fun onCompletion() {
            if (!controlsListener.onNextClicked()) {
                showSeasonFinishDialog()
            }
        }
    }

    private val controlsListener = object : VideoControlsButtonListener {
        override fun onPlayPauseClicked(): Boolean {
            if (binding.player.isPlaying) {
                playerAnalytics.pauseClick()
                binding.player.pause()
            } else {
                playerAnalytics.playClick()
                binding.player.start()
            }
            return true
        }

        override fun onNextClicked(): Boolean {
            playerAnalytics.nextClick(getSeekPercent())
            saveEpisode()
            val episode = getNextEpisode() ?: return false
            playEpisode(episode)
            return true
        }

        override fun onPreviousClicked(): Boolean {
            playerAnalytics.prevClick(getSeekPercent())
            saveEpisode()
            val episode = getPrevEpisode() ?: return false
            playEpisode(episode)
            return true
        }

        override fun onRewindClicked(): Boolean {
            return false
        }

        override fun onFastForwardClicked(): Boolean {
            return false
        }
    }

    private inner class FullScreenListener : View.OnSystemUiVisibilityChangeListener {
        override fun onSystemUiVisibilityChange(visibility: Int) {
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                binding.player.showControls()
            }
        }
    }

    private inner class ControlsVisibilityListener : VideoControlsVisibilityListener {
        override fun onControlsShown() {}

        override fun onControlsHidden() {
            goFullscreen()
        }
    }
}
