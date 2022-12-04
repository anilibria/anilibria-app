package ru.radiationx.data.entity.domain.release

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BlockedInfo(
    val isBlocked: Boolean,
    val reason: String?
) : Parcelable