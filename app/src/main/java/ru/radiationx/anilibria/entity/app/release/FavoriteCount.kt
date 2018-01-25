package ru.radiationx.anilibria.entity.app.release

/**
 * Created by radiationx on 25.01.18.
 */
class FavoriteCount {
    var id: Int = 0
    var isFaved: Boolean = false
    var count: Int = 0
    var isGuest: Boolean = true
    lateinit var sessId: String
    lateinit var skey: String

    var inProgress = false
}