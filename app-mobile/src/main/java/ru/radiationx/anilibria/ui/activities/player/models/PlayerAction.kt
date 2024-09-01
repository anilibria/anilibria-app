package ru.radiationx.anilibria.ui.activities.player.models

import ru.radiationx.anilibria.ui.activities.player.controllers.PlayerSettingsState
import ru.radiationx.data.entity.domain.types.EpisodeId

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

    data class ShowSettings(
        val state: PlayerSettingsState,
    ) : PlayerAction()

    data object ShowPlaylist : PlayerAction()
}