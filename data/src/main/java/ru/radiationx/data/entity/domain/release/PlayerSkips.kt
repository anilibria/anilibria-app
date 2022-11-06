package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

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