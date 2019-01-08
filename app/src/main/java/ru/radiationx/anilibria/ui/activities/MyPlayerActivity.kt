package ru.radiationx.anilibria.ui.activities

import android.app.ActivityManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ContextThemeWrapper
import android.util.Log
import android.view.*
import com.devbrackets.android.exomedia.core.video.scale.ScaleType
import com.devbrackets.android.exomedia.listener.*
import com.devbrackets.android.exomedia.ui.widget.VideoControlsCore
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_myplayer.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.holders.PreferencesHolder
import ru.radiationx.anilibria.model.repository.VitalRepository
import ru.radiationx.anilibria.ui.widgets.VideoControlsAlib
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit


class MyPlayerActivity : AppCompatActivity() {

    companion object {
        const val ARG_RELEASE = "release"
        //const val ARG_CURRENT = "current"
        const val ARG_EPISODE_ID = "episode_id"
        const val ARG_QUALITY = "quality"
        const val ARG_PLAY_FLAG = "play_flag"

        const val VAL_QUALITY_SD = 0
        const val VAL_QUALITY_HD = 1

        const val PLAY_FLAG_DEFAULT = 0
        const val PLAY_FLAG_FORCE_START = 1
        const val PLAY_FLAG_FORCE_CONTINUE = 2


        //private const val NOT_SELECTED = -1
        private const val NO_ID = -1

        private const val DEFAULT_QUALITY = VAL_QUALITY_SD
    }

    private lateinit var releaseData: ReleaseFull
    private var playFlag = PLAY_FLAG_DEFAULT
    private var currentEpisodeId = NO_ID
    //private var currentEpisode = NOT_SELECTED
    private var quality = DEFAULT_QUALITY
    private var videoControls: VideoControlsAlib? = null
    private val fullScreenListener = FullScreenListener()
    private val vitalRepository: VitalRepository = App.injections.vitalRepository
    private val releaseInteractor = App.injections.releaseInteractor
    private val currentVitals = mutableListOf<VitalItem>()
    private val flagsHelper = PlayerWindowFlagHelper
    private var fullscreenOrientation = false

    private var currentFullscreen = false
    private var currentOrientation: Int = Configuration.ORIENTATION_UNDEFINED

    private var compositeDisposable = CompositeDisposable()
    private val scales = listOf(
            ScaleType.CENTER_CROP,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_XY
    )
    private val defaultScale = ScaleType.FIT_CENTER
    private var currentScale = defaultScale

    fun Disposable.addToDisposable() {
        compositeDisposable.add(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadVital()
        initUiFlags()
        currentOrientation = resources.configuration.orientation
        updateUiFlags()
        setContentView(R.layout.activity_myplayer)

        intent?.let {
            val release = it.getSerializableExtra(ARG_RELEASE) as ReleaseFull?
            release?.let {
                this.releaseData = it
            }
            currentEpisodeId = it.getIntExtra(ARG_EPISODE_ID, if (releaseData.episodes.size > 0) 0 else NO_ID)
            //currentEpisode = it.getIntExtra(ARG_CURRENT, if (releaseData.episodes.size > 0) 0 else NOT_SELECTED)
            quality = it.getIntExtra(ARG_QUALITY, DEFAULT_QUALITY)
            playFlag = it.getIntExtra(ARG_PLAY_FLAG, PLAY_FLAG_DEFAULT)
        }

        player.setScaleType(currentScale)
        player.setOnPreparedListener(playerListener)
        player.setOnCompletionListener(playerListener)
        //player.setOnErrorListener(playerListener)

        videoControls = VideoControlsAlib(ContextThemeWrapper(this, this.theme), null, 0)

        videoControls?.apply {
            setScale(currentScale, false)
            setQuality(quality)
            setTitle(releaseData.title)
            player.setControls(this as VideoControlsCore)
            setOpeningListener(alibControlListener)
            setVisibilityListener(ControlsVisibilityListener())
            let {
                it.setNextButtonRemoved(false)
                it.setPreviousButtonRemoved(false)
                it.setButtonListener(controlsListener)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(ActivityManager.TaskDescription(releaseData.title))
        }

        playEpisode(getEpisode())
        updateUiFlags()
    }

    private fun loadScale(orientation: Int): ScaleType {
        val scaleOrdinal = App.injections.defaultPreferences.getInt("video_scale_$orientation", defaultScale.ordinal)
        return ScaleType.fromOrdinal(scaleOrdinal)
    }

    private fun saveScale(orientation: Int, scale: ScaleType) {
        App.injections.defaultPreferences.edit().putInt("video_scale_$orientation", scale.ordinal).apply()
    }

    private fun toggleScale() {
        val currentIndex = scales.indexOf(currentScale)

        val targetIndex = if (currentIndex == scales.lastIndex) {
            0
        } else {
            currentIndex + 1
        }
        updateScale(scales[targetIndex], true)
    }

    private fun updateScale(scale: ScaleType, fromUser: Boolean) {
        val inMultiWindow = getInMultiWindow()
        Log.d("MyPlayer", "updateScale $currentScale, $scale, $inMultiWindow")
        currentScale = scale
        if (!inMultiWindow) {
            saveScale(currentOrientation, currentScale)
        }
        videoControls?.setScaleEnabled(!inMultiWindow)
        videoControls?.setScale(currentScale, fromUser)
        player?.setScaleType(currentScale)
    }

    private fun loadVital() {
        vitalRepository
                .observeByRule(VitalItem.Rule.VIDEO_PLAYER)
                .subscribe {
                    it.filter { it.events.contains(VitalItem.EVENT.EXIT_VIDEO) && it.type == VitalItem.VitalType.FULLSCREEN }.let {
                        if (it.isNotEmpty()) {
                            showVitalItems(it)
                        }
                    }
                }
                .addToDisposable()
    }

    fun showVitalItems(vital: List<VitalItem>) {
        currentVitals.clear()
        currentVitals.addAll(vital)
    }

    private fun showQualityDialog() {
        AlertDialog.Builder(this)
                .setTitle("Качество")
                .setSingleChoiceItems(arrayOf("SD", "HD"), when (quality) {
                    MyPlayerActivity.VAL_QUALITY_SD -> 0
                    MyPlayerActivity.VAL_QUALITY_HD -> 1
                    else -> -1
                }) { p0, p1 ->
                    val quality: Int = when (p1) {
                        0 -> MyPlayerActivity.VAL_QUALITY_SD
                        1 -> MyPlayerActivity.VAL_QUALITY_HD
                        else -> -1
                    }
                    if (quality != -1) {
                        this.quality = quality
                        App.injections.appPreferences.setQuality(when (quality) {
                            MyPlayerActivity.VAL_QUALITY_SD -> PreferencesHolder.QUALITY_SD
                            MyPlayerActivity.VAL_QUALITY_HD -> PreferencesHolder.QUALITY_HD
                            else -> PreferencesHolder.QUALITY_NO
                        })
                        videoControls?.setQuality(this.quality)
                        saveEpisode()
                        playEpisode(getEpisode())
                    }
                    p0.dismiss()
                }
                .show()
    }

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    private fun getInMultiWindow(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isInMultiWindowMode
        } else {
            false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        newConfig?.also { config ->
            val correctOrientation = config.orientation
            fullscreenOrientation = when (correctOrientation) {
                Configuration.ORIENTATION_LANDSCAPE -> true
                else -> false
            }
            currentOrientation = correctOrientation
            updateUiFlags()
            videoControls?.setFullScreenMode(fullscreenOrientation)
        }

    }

    private fun correctOrientation(orientation: Int): Int {
        val inMultiWindow = getInMultiWindow()
        return if (inMultiWindow) {
            Configuration.ORIENTATION_PORTRAIT
        } else {
            orientation
        }
        /*return when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> if (inMultiWindow) Configuration.ORIENTATION_PORTRAIT else orientation
            else -> if (inMultiWindow) Configuration.ORIENTATION_LANDSCAPE else orientation
        }*/
    }

    private fun saveEpisode() {
        releaseInteractor.putEpisode(getEpisode().apply {
            Log.e("SUKA", "Set posistion seek: ${player.currentPosition}")
            seek = player.currentPosition
            isViewed = true
        })
    }


    private val random = Random()

    private fun rand(from: Int, to: Int): Int {
        return random.nextInt(to - from) + from
    }

    override fun onDestroy() {
        saveEpisode()
        compositeDisposable.dispose()
        player.stopPlayback()
        super.onDestroy()
        exitFullscreen()
        if (currentVitals.isNotEmpty()) {
            val randomVital = if (currentVitals.size > 1) rand(0, currentVitals.size) else 0
            val listItem = currentVitals[randomVital]
            startActivity(Intent(App.instance, FullScreenActivity::class.java).apply {
                putExtra(FullScreenActivity.VITAL_ITEM, listItem)
            })
        }
    }

    private fun checkIndex(id: Int): Boolean {
        val lastId = releaseData.episodes.last().id
        val firstId = releaseData.episodes.first().id
        return id in lastId..firstId
    }

    private fun getNextEpisode(): ReleaseFull.Episode? {
        val nextId = currentEpisodeId + 1
        if (checkIndex(nextId)) {
            Log.e("S_DEF_LOG", "NEXT INDEX " + nextId)
            return getEpisode(nextId)
        }
        return null
    }

    private fun getPrevEpisode(): ReleaseFull.Episode? {
        val prevId = currentEpisodeId - 1
        if (checkIndex(prevId)) {
            Log.e("S_DEF_LOG", "PREV INDEX " + prevId)
            return getEpisode(prevId)
        }
        return null
    }

    private fun getEpisode(id: Int = currentEpisodeId) = releaseData.episodes.first { it.id == id }

    private fun getEpisodeId(episode: ReleaseFull.Episode) = releaseData.episodes.first { it == episode }.id

    private fun playEpisode(episode: ReleaseFull.Episode) {
        when (playFlag) {
            PLAY_FLAG_DEFAULT -> {
                hardPlayEpisode(episode)
                if (episode.seek > 0) {
                    hardPlayEpisode(episode)
                    val titles = arrayOf("К началу", "К последней позиции")
                    AlertDialog.Builder(this)
                            .setTitle("Перемотать")
                            .setItems(titles) { dialog, which ->
                                if (which == 1) {
                                    player.seekTo(episode.seek)
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
                player.seekTo(episode.seek)
            }
        }
    }

    private fun hardPlayEpisode(episode: ReleaseFull.Episode) {
        supportActionBar?.subtitle = episode.title
        currentEpisodeId = getEpisodeId(episode)
        if (quality == VAL_QUALITY_SD) {
            player.setVideoPath(episode.urlSd?.replace("http:", "https:"))
        } else if (quality == VAL_QUALITY_HD) {
            player.setVideoPath(episode.urlHd?.replace("http:", "https:"))
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
        var flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_VISIBLE

        flags = flags or (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

        window.decorView.also {
            //it.systemUiVisibility = flags
            it.setOnSystemUiVisibilityChangeListener(fullScreenListener)
        }
    }

    private fun updateUiFlags() {
        val scale = loadScale(currentOrientation)
        val inMultiWindow = getInMultiWindow()

        updateScale(if (inMultiWindow) defaultScale else scale, false)

        window.decorView.also {
            it.systemUiVisibility = flagsHelper.getFlags(currentOrientation, currentFullscreen)
        }

        videoControls?.fitSystemWindows(inMultiWindow || currentOrientation != Configuration.ORIENTATION_LANDSCAPE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            window.attributes.layoutInDisplayCutoutMode = if (inMultiWindow) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.attributes = window.attributes
        }
    }


    private val alibControlListener = object : VideoControlsAlib.AlibControlsListener {
        override fun onToolbarBackClick() {
            finish()
        }

        override fun onToolbarQualityClick() {
            showQualityDialog()
        }

        override fun onToolbarScaleClick() {
            toggleScale()
        }

        private val delta = TimeUnit.SECONDS.toMillis(90)
        override fun onMinusClick() {
            val newPosition = player.currentPosition - delta
            player.seekTo(newPosition.coerceIn(0, player.duration))
        }

        override fun onPlusClick() {
            val newPosition = player.currentPosition + delta
            player.seekTo(newPosition.coerceIn(0, player.duration))
        }

        override fun onFullScreenClick() {
            if (fullscreenOrientation) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            fullscreenOrientation = !fullscreenOrientation
            videoControls?.setFullScreenMode(fullscreenOrientation)
        }
    }

    private val playerListener = object : OnPreparedListener, OnCompletionListener, OnErrorListener {
        override fun onPrepared() {
            player.start()
        }

        override fun onCompletion() {
            if (!controlsListener.onNextClicked()) {
                finish()
            }
        }

        override fun onError(e: Exception?): Boolean {
            e?.printStackTrace()
            return false
        }
    }

    private val controlsListener = object : VideoControlsButtonListener {
        override fun onPlayPauseClicked(): Boolean {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.start()
            }
            return true
        }

        override fun onNextClicked(): Boolean {
            saveEpisode()
            val episode = getNextEpisode() ?: return false
            playEpisode(episode)
            return true
        }

        override fun onPreviousClicked(): Boolean {
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
                player.showControls()
            }
        }
    }

    private inner class ControlsVisibilityListener : VideoControlsVisibilityListener {
        override fun onControlsShown() {
            Log.e("MyPlayer", "onControlsShown $supportActionBar, ${supportActionBar?.isShowing}")
        }

        override fun onControlsHidden() {
            Log.e("MyPlayer", "onControlsHidden $supportActionBar")
            goFullscreen()
        }
    }
}
