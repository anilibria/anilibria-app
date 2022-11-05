package ru.radiationx.data.entity.response.release

import java.io.Serializable

data class PlayerSkipsResponse(
    val opening: SkipResponse?,
    val ending: SkipResponse?
) : Serializable {

    data class SkipResponse(
        val start: Long,
        val end: Long
    ) : Serializable
}