package ru.radiationx.data.entity.app.release

import java.io.Serializable

/**
 * Created by radiationx on 25.01.18.
 */
data class FavoriteInfo(
    val rating: Int,
    val isAdded: Boolean
) : Serializable