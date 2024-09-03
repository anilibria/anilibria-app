package ru.radiationx.data.apinext.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.UserId

@Parcelize
data class User(
    val id: UserId,
    // todo API2 nullable nick wtf
    val nickname: String?,
    val avatar: String?
) : Parcelable