package ru.radiationx.data.entity.app.vital

import java.io.Serializable

/**
 * Created by radiationx on 27.01.18.
 */
class VitalItem : Serializable {
    var id: Int = 0
    var name: String? = null
    var type: VitalType = VitalType.BANNER
    var contentType: ContentType = ContentType.WEB
    var contentText: String? = null
    var contentImage: String? = null
    var contentLink: String? = null
    val rules = mutableListOf<Rule>()
    val events = mutableListOf<EVENT>()

    enum class VitalType {
        BANNER,
        FULLSCREEN,
        CONTENT_ITEM
    }

    enum class ContentType {
        WEB,
        IMAGE
    }

    enum class Rule {
        RELEASE_DETAIL,
        RELEASE_LIST,
        VIDEO_PLAYER
    }

    enum class EVENT {
        EXIT_VIDEO
    }
}