package ru.radiationx.anilibria.entity.app.release

/**
 * Created by radiationx on 18.01.18.
 */
class Comment {
    var id: Int = 0
    var forumId: Int = 0
    var topicId: Int = 0
    var date: String? = null
    var message: String? = null
    var authorId: Int = 0
    var authorNick: String? = null
    var avatar: String? = null
}