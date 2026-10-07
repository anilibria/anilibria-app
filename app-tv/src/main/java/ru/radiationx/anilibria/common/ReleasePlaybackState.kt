package ru.radiationx.anilibria.common

import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.shared.ktx.asTimeSecString
import java.util.Date

internal data class ReleasePlaybackState(
    val hasProgress: Boolean,
    val allEpisodesViewed: Boolean,
    val continueEpisodeId: EpisodeId?,
    val continueText: String?,
    val showPlayAction: Boolean,
) {
    companion object {
        // Episode ids are supplied in playback order, starting with the first episode.
        fun resolve(episodeIds: List<EpisodeId>, accesses: List<EpisodeAccess>): ReleasePlaybackState {
            val byId = accesses.associateBy { it.id }
            val allViewed = episodeIds.isNotEmpty() && episodeIds.all { byId[it]?.isViewed == true }
            val availableAccesses = episodeIds.mapNotNull { byId[it] }
            val latestProgress = availableAccesses.filter { it.hasProgress }
                .maxByOrNull { it.lastAccessRaw }
            val lastWatched = availableAccesses.lastOrNull { it.isViewed }
            val anchor = latestProgress ?: lastWatched
            val continueId = when {
                allViewed -> episodeIds.first()
                anchor == null -> null
                !anchor.isViewed -> anchor.id
                else -> episodeIds.drop(episodeIds.indexOf(anchor.id) + 1)
                    .firstOrNull { byId[it]?.isViewed != true }
                    ?: episodeIds.firstOrNull { byId[it]?.isViewed != true }
            }
            val seek = continueId?.let { byId[it] }?.takeIf { !it.isViewed }?.seek ?: 0L
            val continueText = when {
                allViewed -> "Заново"
                continueId == null -> null
                episodeIds.size == 1 -> "Продолжить (${Date(seek).asTimeSecString()})"
                seek > 0L -> "Продолжить (${continueId.id} серия - ${Date(seek).asTimeSecString()})"
                else -> "Продолжить (${continueId.id} серия)"
            }
            return ReleasePlaybackState(
                hasProgress = latestProgress != null,
                allEpisodesViewed = allViewed,
                continueEpisodeId = continueId,
                continueText = continueText,
                showPlayAction = episodeIds.isNotEmpty() && (episodeIds.size > 1 || continueId == null),
            )
        }
    }
}
