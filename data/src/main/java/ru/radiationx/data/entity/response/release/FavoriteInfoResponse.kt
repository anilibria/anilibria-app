package ru.radiationx.data.entity.response.release

import java.io.Serializable

/**
 * Created by radiationx on 25.01.18.
 */
data class FavoriteInfoResponse(
    val rating: Int,
    val isAdded: Boolean
) : Serializable