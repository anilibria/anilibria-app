package ru.radiationx.data.apinext.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReleaseCounters(
    val favorites: Int,
    val planned: Int,
    val watched: Int,
    val watching: Int,
    val postponed: Int,
    val abandoned: Int
) : Parcelable