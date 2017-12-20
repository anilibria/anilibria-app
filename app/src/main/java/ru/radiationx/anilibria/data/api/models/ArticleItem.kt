package ru.radiationx.anilibria.data.api.models

import java.io.Serializable

/**
 * Created by radiationx on 18.12.17.
 */
class ArticleItem : Serializable {
    var elementId: Int = 0

    lateinit var url: String
    lateinit var title: String

    var userId: Int = 0
    lateinit var userNick: String

    lateinit var imageUrl: String
    var imageWidth: Int = -1
    var imageHeight: Int = -1
    lateinit var content: String

    var otherUrl: String? = null
    var viewsCount: Int = 0
    var commentsCount: Int = 0;
}