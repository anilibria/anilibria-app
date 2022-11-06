package ru.radiationx.data.entity.domain.release

import java.io.Serializable

data class PlayerSkips(
    val opening: Skip?,
    val ending: Skip?
) : Serializable {

    data class Skip(
        val start: Long,
        val end: Long
    ) : Serializable
}