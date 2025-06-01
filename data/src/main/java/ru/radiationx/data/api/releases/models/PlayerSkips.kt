package ru.radiationx.data.api.releases.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerSkips(
    val opening: Skip?,
    val ending: Skip?
) : Parcelable {

    @Parcelize
    data class Skip(
        val start: Long,
        val end: Long
    ) : Parcelable
}