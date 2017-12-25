package ru.radiationx.anilibria.ui.activities

import android.annotation.TargetApi
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
import kotlinx.android.synthetic.main.activity_myplayer.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.release.ReleaseFull
import java.lang.Exception


class MyPlayerActivity : AppCompatActivity(), OnPreparedListener, OnCompletionListener, OnErrorListener, VideoControlsButtonListener {

    companion object {
        const val ARG_RELEASE = "episodes"
        const val ARG_CURRENT = "current"
        const val ARG_QUALITY = "quality"
        const val VAL_QUALITY_SD = 0
        const val VAL_QUALITY_HD = 1
        private const val NOT_SELECTED = -1
        private const val DEFAULT_QUALITY = VAL_QUALITY_SD
    }

    private lateinit var releaseData: ReleaseFull
    private var currentEpisode = NOT_SELECTED
    private var quality = DEFAULT_QUALITY
    private lateinit var videoControls: VideoControls
    private val fullScreenListener = FullScreenListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUiFlags()
        setContentView(R.layout.activity_myplayer)

        intent?.let {
            val release = it.getSerializableExtra(ARG_RELEASE) as ReleaseFull?
            release?.let {
                this.releaseData = it
            }
            currentEpisode = it.getIntExtra(ARG_CURRENT, if (releaseData.episodes.size > 0) 0 else NOT_SELECTED)
            quality = it.getIntExtra(ARG_QUALITY, DEFAULT_QUALITY)
        }


        player.setOnPreparedListener(this)

        player.videoControls?.let { videoControls = it }

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
        playEpisode(getCurrentEpisode())
        supportActionBar?.title = releaseData.title
        supportActionBar?.elevation = 0f
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Качество")
                .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_settings))
                .setOnMenuItemClickListener {
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
                                    playEpisode(getCurrentEpisode())
                                }
                                p0.dismiss()
                            })
                            .show()
                    true
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
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

    override fun onDestroy() {
        super.onDestroy()
        exitFullscreen()
        player.stopPlayback()
    }

    private fun checkIndex(index: Int): Boolean = index >= 0 && index < releaseData.episodes.size

    private fun getNextEpisode(): ReleaseFull.Episode? {
        Log.e("SUKA", "CLICK NEXT " + currentEpisode)
        val nextIndex = currentEpisode - 1
        if (checkIndex(nextIndex)) {
            Log.e("SUKA", "NEXT INDEX " + nextIndex)
            currentEpisode = nextIndex
            return getCurrentEpisode()
        }
        return null
    }

    private fun getPrevEpisode(): ReleaseFull.Episode? {
        val prevIndex = currentEpisode + 1
        if (checkIndex(prevIndex)) {
            Log.e("SUKA", "PREV INDEX " + prevIndex)
            currentEpisode = prevIndex
            return getCurrentEpisode()
        }
        return null
    }

    private fun getCurrentEpisode(): ReleaseFull.Episode = releaseData.episodes[currentEpisode]

    private fun playEpisode(episode: ReleaseFull.Episode) {
        supportActionBar?.subtitle = episode.title
        if (quality == VAL_QUALITY_SD) {
            Log.e("SUKA", "playEpisode " + episode.urlSd)
            player.setVideoPath(episode.urlSd)
        } else if (quality == VAL_QUALITY_HD) {
            Log.e("SUKA", "playEpisode " + episode.urlHd)
            player.setVideoPath(episode.urlHd)
        }
    }


    /* Video player callbacks*/
    override fun onPrepared() {
        player.start()
    }

    override fun onCompletion() {

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
        val episode = getNextEpisode() ?: return false
        playEpisode(episode)
        return true
    }

    override fun onPreviousClicked(): Boolean {
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
