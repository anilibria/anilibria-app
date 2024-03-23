package ru.radiationx.anilibria.ui.activities.player.models

import ru.radiationx.data.entity.domain.release.Episode
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId

data class PlayerData(
    val releases: List<PlayerRelease>,
) {
    val episodes: List<Episode> = releases.flatMap { it.episodes }

    fun getRelease(releaseId: ReleaseId): PlayerRelease {
        return requireNotNull(releases.find { it.id == releaseId }) {
            "No loaded release for id ${releaseId.id} in ${releases.map { it.id.id }}"
        }
    }

    fun getEpisode(episodeId: EpisodeId): Episode {
        return requireNotNull(episodes.find { it.id == episodeId }) {
            "No loaded episode for id $episodeId"
        }
    }

}
