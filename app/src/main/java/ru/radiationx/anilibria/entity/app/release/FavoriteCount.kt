package ru.radiationx.anilibria.entity.app.release

import java.io.Serializable

/**
 * Created by radiationx on 25.01.18.
 */
class FavoriteCount : Serializable {
    var id: Int = 0
    var isFaved: Boolean = false
    var count: Int = 0
    var isGuest: Boolean = true
    lateinit var sessId: String
    lateinit var skey: String

    var inProgress = false
}