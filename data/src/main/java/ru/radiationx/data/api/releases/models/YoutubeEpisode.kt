package ru.radiationx.data.api.releases.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.EpisodeId
import ru.radiationx.data.common.EpisodeUUID
import ru.radiationx.data.common.Url
import java.util.Date

@Parcelize
data class YoutubeEpisode(
    val uuid: EpisodeUUID,
    val id: EpisodeId,
    val title: String?,
    val updatedAt: Date?,
    val youtubeId: String,
    val url: Url.Absolute
) : Parcelable