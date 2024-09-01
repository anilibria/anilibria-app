package ru.radiationx.data.apinext.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    val nickname: String,
    val avatar: String?
) : Parcelable