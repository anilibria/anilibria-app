package ru.radiationx.anilibria.entity.app.release

import java.io.Serializable

/**
 * Created by radiationx on 25.01.18.
 */
class FavoriteInfo : Serializable {
    var rating: Int = 0
    var isAdded: Boolean = false

    var inProgress = false
}