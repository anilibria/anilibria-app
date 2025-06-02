package ru.radiationx.data.api.profile.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.common.Url
import ru.radiationx.data.common.UserId

@Parcelize
data class User(
    val id: UserId,
    val nickname: String?,
    val avatar: Url.Path?
) : Parcelable