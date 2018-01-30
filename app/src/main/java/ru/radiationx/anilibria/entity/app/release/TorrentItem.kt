package ru.radiationx.anilibria.entity.app.release

import java.io.Serializable

/**
 * Created by radiationx on 30.01.18.
 */
class TorrentItem : Serializable {
    lateinit var episode: String
    lateinit var quality: String
    lateinit var size: String
    lateinit var url: String
}