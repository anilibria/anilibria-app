package ru.radiationx.data.entity.app.other

import ru.radiationx.data.entity.common.AuthState

class ProfileItem {

    companion object {
        const val NO_ID = -1
        const val NO_VALUE = ""
    }

    var authState: AuthState = AuthState.NO_AUTH
    var avatarUrl: String? = NO_VALUE
    var nick: String = NO_VALUE
    var id: Int = NO_ID

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProfileItem

        if (authState != other.authState) return false
        if (avatarUrl != other.avatarUrl) return false
        if (nick != other.nick) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authState.hashCode()
        result = 31 * result + (avatarUrl?.hashCode() ?: 0)
        result = 31 * result + nick.hashCode()
        result = 31 * result + id
        return result
    }
}
