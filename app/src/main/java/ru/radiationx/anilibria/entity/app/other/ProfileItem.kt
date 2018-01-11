package ru.radiationx.anilibria.entity.app.other

import ru.radiationx.anilibria.entity.common.AuthState

class ProfileItem {
    lateinit var authState: AuthState
    var avatarUrl: String? = null
    var nick: String? = null
    var id: Int = 0
}
