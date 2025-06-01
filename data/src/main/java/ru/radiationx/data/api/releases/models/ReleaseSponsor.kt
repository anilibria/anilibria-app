package ru.radiationx.data.api.releases.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.Url

@Parcelize
data class ReleaseSponsor(
    val id: String,
    val title: String,
    val description: String,
    val urlTitle: String?,
    val url: Url.Absolute?
) : Parcelable