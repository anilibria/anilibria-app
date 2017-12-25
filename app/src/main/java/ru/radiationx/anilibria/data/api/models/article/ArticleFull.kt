package ru.radiationx.anilibria.data.api.models.article

/**
 * Created by radiationx on 18.12.17.
 */
class ArticleFull {
    lateinit var title: String
    lateinit var content: String
    var userId: Int = 0
    lateinit var userNick: String
    lateinit var date: String
}
