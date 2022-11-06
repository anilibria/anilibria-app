package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FavoriteInfo(
    val rating: Int,
    val isAdded: Boolean
) : Parcelable