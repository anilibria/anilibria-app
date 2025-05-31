package ru.radiationx.data.apinext.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.common.Url

@Parcelize
data class ReleaseSponsor(
    val id: String,
    val title: String,
    val description: String,
    val urlTitle: String?,
    val url: Url.Absolute?
) : Parcelable