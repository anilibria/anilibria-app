package ru.radiationx.data.api.releases.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.data.common.EpisodeUUID

@Parcelize
data class ComboEpisode(
    val uuid: EpisodeUUID,
    val id: EpisodeId,
    val episode: Episode?,
    val youtubeEpisode: YoutubeEpisode?,
    val rutubeEpisode: RutubeEpisode?
) : Parcelable
