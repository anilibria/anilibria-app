package ru.radiationx.anilibria.screen.player

import android.os.Bundle
import android.view.View
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.Player
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
        private const val ARG_RESTART = "restart"

        fun newInstance(
            releaseId: ReleaseId,
            episodeId: EpisodeId?,
            restart: Boolean = false,
        ): PlayerFragment = PlayerFragment().putExtra {
            putParcelable(ARG_RELEASE_ID, releaseId)
            putParcelable(ARG_EPISODE_ID, episodeId)
            putBoolean(ARG_RESTART, restart)
        }
    }

    private val viewModel by viewModel<PlayerViewModel> {
        PlayerExtra(
            releaseId = getExtraNotNull(ARG_RELEASE_ID),
            episodeId = getExtra(ARG_EPISODE_ID),
            restart = arguments?.getBoolean(ARG_RESTART) ?: false,
        )
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(viewModel)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    val currentPlayer = player
                    val video = viewModel.videoData.value
                    if (currentPlayer != null && video != null && !isPlaybackSeeking &&
                        currentPlayer.playbackState == Player.STATE_READY &&
                        currentPlayer.currentMediaItem?.localConfiguration?.uri?.toString() == video.url
                    ) {
                        viewModel.onPlaybackProgress(video.episodeId, currentPlayer.currentPosition, currentPlayer.duration)
                    }
                    delay(1000L)
                }
            }
        }

        playerGlue?.actionListener = object : VideoPlayerGlue.OnActionClickedListener {

            override fun onPrevious() = viewModel.onPrevClick(getPosition())
            override fun onNext() = viewModel.onNextClick(getPosition())
            override fun onQualityClick() = viewModel.onQualityClick(getPosition())
            override fun onSpeedClick() = viewModel.onSpeedClick()
            override fun onEpisodesClick() = viewModel.onEpisodesClick(getPosition())
        }
        progressBarManager.initialDelay = 0
        progressBarManager.show()

        subscribeTo(viewModel.videoData.filterNotNull()) {
            progressBarManager.hide()
            playerGlue?.apply {
                title = it.title
                subtitle = it.subtitle
                seekTo(it.seek)
                preparePlayer(it.url)
            }
            skipsPart?.setSkips(it.skips)
        }

        subscribeTo(viewModel.playAction.filterNotNull()) {
            if (it) {
                playerGlue?.play()
            } else {
                playerGlue?.pause()
            }
        }

        subscribeTo(viewModel.speedState.filterNotNull()) {
            player?.playbackParameters = PlaybackParameters(it)
        }

        subscribeTo(viewModel.qualityState.filterNotNull()) {
            playerGlue?.setQuality(it)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isPlaybackSeeking) viewModel.onPauseClick(getPosition())
    }

    override fun onCompletePlaying() {
        viewModel.onComplete(getPosition())
    }

    override fun onPreparePlaying() {
        viewModel.onPrepare(getDuration())
    }

    private fun getPosition(): Long = player?.currentPosition ?: 0

    private fun getDuration(): Long = player?.duration ?: 0
}
