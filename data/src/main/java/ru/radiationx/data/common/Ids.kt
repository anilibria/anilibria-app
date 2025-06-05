package ru.radiationx.data.common

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
data class EpisodeUUID(val uuid: String) : Parcelable

@Parcelize
data class YoutubeId(val id: Int) : Parcelable

@Parcelize
data class FeedId(val releaseId: ReleaseId?, val youtubeId: YoutubeId?) : Parcelable

@Parcelize
data class TorrentId(val id: Int, val releaseId: ReleaseId) : Parcelable

@Parcelize
data class FranchiseId(val id: String) : Parcelable

@Parcelize
data class GenreId(val id: Int) : Parcelable