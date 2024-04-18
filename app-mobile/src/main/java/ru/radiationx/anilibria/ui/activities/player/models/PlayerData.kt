package ru.radiationx.anilibria.ui.activities.player.models

import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId

data class PlayerData(
    val releases: List<PlayerRelease>,
) {
    val episodes: List<Episode> = releases.flatMap { it.episodes }

    fun getRelease(releaseId: ReleaseId): PlayerRelease? {
        return releases.find { it.id == releaseId }
    }

    fun getEpisode(episodeId: EpisodeId): Episode? {
        return episodes.find { it.id == episodeId }
    }

}
