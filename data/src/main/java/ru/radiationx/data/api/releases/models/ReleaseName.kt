package ru.radiationx.data.api.releases.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReleaseName(
    val main: String,
    val english: String,
    val alternative: String?
) : Parcelable