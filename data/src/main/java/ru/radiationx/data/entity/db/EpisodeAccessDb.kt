package ru.radiationx.data.entity.db

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
data class EpisodeAccessDb(
    @Json(name = "id") val id: Int,
    @Json(name = "releaseId") val releaseId: Int,
    @Json(name = "seek") val seek: Long,
    @Json(name = "isViewed") val isViewed: Boolean,
    @Json(name = "lastAccess") val lastAccess: Long,
) 