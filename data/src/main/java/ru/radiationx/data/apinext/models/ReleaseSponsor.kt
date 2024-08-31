package ru.radiationx.data.apinext.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReleaseSponsor(
    val id: String,
    val title: String,
    val description: String,
    val urlTitle: String,
    val url: String
) : Parcelable