package ru.radiationx.data.apinext.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.radiationx.data.entity.domain.types.UserId

@Parcelize
data class ReleaseMember(
    val id: String,
    val user: User?,
    val role: Role,
    val nickname: String
) : Parcelable {

    @Parcelize
    data class Role(
        val value: String,
        val description: String
    ) : Parcelable

    @Parcelize
    data class User(
        val id: UserId,
        val avatar: RelativeUrl?
    ) : Parcelable
}