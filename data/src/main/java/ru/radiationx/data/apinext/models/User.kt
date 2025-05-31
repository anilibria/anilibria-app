package ru.radiationx.data.apinext.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.common.Url
import ru.radiationx.data.entity.domain.types.UserId

@Parcelize
data class User(
    val id: UserId,
    val nickname: String?,
    val avatar: Url.Relative?
) : Parcelable