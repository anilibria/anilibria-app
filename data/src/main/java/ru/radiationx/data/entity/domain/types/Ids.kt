package ru.radiationx.data.entity.domain.types

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class UserId(val id: Int) : Parcelable

@Parcelize
data class ReleaseId(val id: Int) : Parcelable

@Parcelize
data class ReleaseCode(val code: String) : Parcelable

@Parcelize
data class EpisodeId(val id: String, val releaseId: ReleaseId) : Parcelable

@Parcelize
data class YoutubeId(val id: Int) : Parcelable

@Parcelize
data class FeedId(val releaseId: ReleaseId?, val youtubeId: YoutubeId?) : Parcelable

@Parcelize
data class TorrentId(val id: Int, val releaseId: ReleaseId) : Parcelable