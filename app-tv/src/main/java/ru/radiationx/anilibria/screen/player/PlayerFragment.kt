package ru.radiationx.anilibria.screen.player

import android.os.Bundle
import android.view.View
import com.google.android.exoplayer2.PlaybackParameters
import kotlinx.coroutines.flow.filterNotNull
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.subscribeTo

class PlayerFragment : BasePlayerFragment() {

    companion object {

        private const val ARG_RELEASE_ID = "release id"
        private const val ARG_EPISODE_ID = "episode id"

        fun newInstance(
            releaseId: ReleaseId,
            episodeId: EpisodeId?
        ): PlayerFragment = PlayerFragment().putExtra {
            putParcelable(ARG_RELEASE_ID, releaseId)
            putParcelable(ARG_EPISODE_ID, episodeId)
        }
    }

    private val viewModel by viewModel<PlayerViewModel> {
        PlayerExtra(
            releaseId = getExtraNotNull(ARG_RELEASE_ID),
            episodeId = getExtra(ARG_EPISODE_ID)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        playerGlue?.actionListener = object : VideoPlayerGlue.OnActionClickedListener {

            override fun onPrevious() = viewModel.onPrevClick(getPosition())
            override fun onNext() = viewModel.onNextClick(getPosition())
            override fun onQualityClick() = viewModel.onQualityClick(getPosition())
            override fun onSpeedClick() = viewModel.onSpeedClick(getPosition())
            override fun onEpisodesClick() = viewModel.onEpisodesClick(getPosition())
        }

        subscribeTo(viewModel.videoData.filterNotNull()) {
            playerGlue?.apply {
                title = it.title
                subtitle = it.subtitle
                seekTo(it.seek)
                preparePlayer(it.url)
            }
        }

        subscribeTo(viewModel.playAction.filterNotNull()) {
            if (it) {
                playerGlue?.play()
            } else {
                playerGlue?.pause()
            }
        }

        subscribeTo(viewModel.speedState.filterNotNull()) {
            player?.setPlaybackParameters(PlaybackParameters(it))
        }

        subscribeTo(viewModel.qualityState.filterNotNull()) {
            playerGlue?.setQuality(it)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPauseClick(getPosition())
    }

    override fun onCompletePlaying() {
        viewModel.onComplete(getPosition())
    }

    override fun onPreparePlaying() {
        viewModel.onPrepare(getPosition(), getDuration())
    }

    private fun getPosition(): Long = player?.currentPosition ?: 0

    private fun getDuration(): Long = player?.duration ?: 0
}