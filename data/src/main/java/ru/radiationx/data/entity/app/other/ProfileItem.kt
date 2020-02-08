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
}
