package ru.radiationx.data.entity.domain.release

import java.io.Serializable
import java.util.*

/**
 * Created by radiationx on 30.01.18.
 */
data class TorrentItem(
    val id: Int,
    val hash: String?,
    val leechers: Int,
    val seeders: Int,
    val completed: Int,
    val quality: String?,
    val series: String?,
    val size: Long,
    val url: String?,
    val date: Date?
) : Serializable 