package ru.radiationx.anilibria.screen.player

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.exoplayer2.PlaybackParameters
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo
import ru.radiationx.shared_app.di.viewModel

class PlayerFragment : BasePlayerFragment() {

    companion object {

        private const val ARG_RELEASE_ID = "release id"
        private const val ARG_EPISODE_ID = "episode id"

        fun newInstance(releaseId: Int, episodeId: Int = -1): PlayerFragment = PlayerFragment().putExtra {
            putInt(ARG_RELEASE_ID, releaseId)
            putInt(ARG_EPISODE_ID, episodeId)
        }
    }

    private val viewModel by viewModel<PlayerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        arguments?.apply {
            viewModel.argReleaseId = getInt(ARG_RELEASE_ID, viewModel.argReleaseId)
            viewModel.argEpisodeId = getInt(ARG_EPISODE_ID, viewModel.argEpisodeId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerGlue?.actionListener = object : VideoPlayerGlue.OnActionClickedListener {

            override fun onPrevious() = viewModel.onPrevClick(getPosition())
            override fun onNext() = viewModel.onNextClick(getPosition())
            override fun onQualityClick() = viewModel.onQualityClick(getPosition())
            override fun onSpeedClick() = viewModel.onSpeedClick(getPosition())
            override fun onEpisodesClick() = viewModel.onEpisodesClick(getPosition())
        }

        subscribeTo(viewModel.videoData) {
            Log.e("kokoko", "video data $it")
            playerGlue?.apply {
                title = it.title
                subtitle = it.subtitle
                preparePlayer(it.url)
                seekTo(it.seek)
                play()
            }
        }

        subscribeTo(viewModel.speedState) {
            player?.setPlaybackParameters(PlaybackParameters(it))
        }

        subscribeTo(viewModel.qualityState) {
            playerGlue?.setQuality(it)
        }
    }


    override fun onPause() {
        super.onPause()
        viewModel.onPauseClick(getPosition())
    }

    private fun getPosition(): Long = player?.currentPosition ?: 0

}