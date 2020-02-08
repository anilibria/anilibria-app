package ru.radiationx.data.entity.app.release

import java.io.Serializable

/**
 * Created by radiationx on 30.01.18.
 */
class TorrentItem : Serializable {
    var id: Int = 0
    var hash: String? = null
    var leechers: Int = 0
    var seeders: Int = 0
    var completed: Int = 0
    var quality: String? = null
    var series: String? = null
    var size: Long = 0
    var url: String? = null
}