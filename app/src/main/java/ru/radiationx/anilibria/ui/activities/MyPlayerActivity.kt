package ru.radiationx.anilibria.ui.activities

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.devbrackets.android.exomedia.listener.*
import com.devbrackets.android.exomedia.ui.widget.VideoControls
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


class MyPlayerActivity : AppCompatActivity(), OnPreparedListener, OnCompletionListener, OnErrorListener, VideoControlsButtonListener {

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
    private lateinit var videoControls: VideoControlsAlib
    private val fullScreenListener = FullScreenListener()
    private val vitalRepository: VitalRepository = App.injections.vitalRepository
    private val releaseInteractor = App.injections.releaseInteractor
    private val currentVitals = mutableListOf<VitalItem>()
    private var qualityMenuItem: MenuItem? = null

    private var compositeDisposable = CompositeDisposable()

    fun Disposable.addToDisposable() {
        compositeDisposable.add(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadVital()
        initUiFlags()
        setContentView(R.layout.activity_myplayer)

        supportActionBar?.apply {
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this.themedContext, R.color.playerColorPrimary)))
        }

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


        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)

        videoControls = VideoControlsAlib(player.context)
        player.setControls(videoControls as VideoControlsCore)
        //player.videoControls?.let { videoControls = it }

        videoControls.setVisibilityListener(ControlsVisibilityListener())
        videoControls.fitsSystemWindows = false
        videoControls.let {
            it.setNextButtonRemoved(false)
            it.setPreviousButtonRemoved(false)
            //it.setNextButtonEnabled(true)
            //it.setPreviousButtonEnabled(true)
            //it.setPreviousDrawable(ContextCompat.getDrawable(this, R.drawable.ic_blogs))
            //it.setNextDrawable(ContextCompat.getDrawable(this, R.drawable.ic_news))
            it.setButtonListener(this)
        }
        videoControls.setOpeningListener(object : VideoControlsAlib.OpeningButtonsListener {
            private val delta = TimeUnit.SECONDS.toMillis(90)
            override fun onMinusClick() {
                val newPosition = player.currentPosition - delta
                player.seekTo(newPosition.coerceIn(0, player.duration))
            }

            override fun onPlusClick() {
                val newPosition = player.currentPosition + delta
                player.seekTo(newPosition.coerceIn(0, player.duration))
            }
        })
        playEpisode(getEpisode())
        supportActionBar?.title = releaseData.title
        supportActionBar?.elevation = 0f
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        qualityMenuItem = menu.add("Качество")
                .setIcon(getQualityIcon())
                .setOnMenuItemClickListener {
                    showQualityDialog()
                    true
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    private fun getQualityIcon(): Drawable? {
        val iconRes = when (quality) {
            VAL_QUALITY_SD -> R.drawable.ic_quality_sd
            VAL_QUALITY_HD -> R.drawable.ic_quality_hd
            else -> R.drawable.ic_toolbar_settings
        }
        return ContextCompat.getDrawable(this, iconRes)?.apply {
            setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun showQualityDialog() {
        AlertDialog.Builder(this)
                .setTitle("Качество")
                .setSingleChoiceItems(arrayOf("SD", "HD"), when (quality) {
                    MyPlayerActivity.VAL_QUALITY_SD -> 0
                    MyPlayerActivity.VAL_QUALITY_HD -> 1
                    else -> -1
                }, { p0, p1 ->
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
                        qualityMenuItem?.icon = getQualityIcon()
                        saveEpisode()
                        playEpisode(getEpisode())
                    }
                    p0.dismiss()
                })
                .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        player.pause()
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
            player.setVideoPath(episode.urlSd)
        } else if (quality == VAL_QUALITY_HD) {
            player.setVideoPath(episode.urlHd)
        }
    }


    /* Video player callbacks*/
    override fun onPrepared() {
        player.start()
    }

    override fun onCompletion() {
        if (!onNextClicked()) {
            finish()
        }
    }

    override fun onError(e: Exception?): Boolean {
        e?.printStackTrace()
        return false
    }

    /* Controls callbacks */
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


//    dksa;lf


    private fun goFullscreen() {
        setUiFlags(true)
    }

    private fun exitFullscreen() {
        setUiFlags(false)
    }

    /**
     * Correctly sets up the fullscreen flags to avoid popping when we switch
     * between fullscreen and not
     */
    private fun initUiFlags() {
        var flags = View.SYSTEM_UI_FLAG_VISIBLE

        flags = flags or (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

        val decorView = window.decorView
        if (decorView != null) {
            decorView.systemUiVisibility = flags
            decorView.setOnSystemUiVisibilityChangeListener(fullScreenListener)
        }
    }

    /**
     * Applies the correct flags to the windows decor view to enter
     * or exit fullscreen mode
     *
     * @param fullscreen True if entering fullscreen mode
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private fun setUiFlags(fullscreen: Boolean) {
        val decorView = window.decorView
        if (decorView != null) {
            decorView.systemUiVisibility = if (fullscreen) getFullscreenUiFlags() else View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    /**
     * Determines the appropriate fullscreen flags based on the
     * systems API version.
     *
     * @return The appropriate decor view flags to enter fullscreen mode when supported
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private fun getFullscreenUiFlags(): Int {
        var flags = View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        flags = flags or (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        return flags
    }

    /**
     * Listens to the system to determine when to show the default controls
     * for the [VideoView]
     */
    private inner class FullScreenListener : View.OnSystemUiVisibilityChangeListener {
        override fun onSystemUiVisibilityChange(visibility: Int) {
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                player.showControls()
            }
        }
    }

    /**
     * A Listener for the [VideoControls]
     * so that we can re-enter fullscreen mode when the controls are hidden.
     */
    private inner class ControlsVisibilityListener : VideoControlsVisibilityListener {
        override fun onControlsShown() {
            // No additional functionality performed
            supportActionBar?.show()
        }

        override fun onControlsHidden() {
            goFullscreen()
            supportActionBar?.hide()
        }
    }
}
