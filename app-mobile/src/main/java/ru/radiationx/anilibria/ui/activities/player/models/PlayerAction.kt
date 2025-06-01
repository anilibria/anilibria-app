package ru.radiationx.anilibria.ui.activities.player.models

import ru.radiationx.data.common.EpisodeId

sealed class PlayerAction {
    data class PlayEpisode(
        val episodes: List<EpisodeState>,
        val episodeId: EpisodeId,
        val seek: Long,
    ) : PlayerAction()

    data class PlaylistChange(
        val episodes: List<EpisodeState>,
    ) : PlayerAction()

    data class Play(
        val seek: Long?,
    ) : PlayerAction()

    data object ShowPlaylist : PlayerAction()
}