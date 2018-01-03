package ru.radiationx.anilibria.entity.app.profile

/**
 * Created by radiationx on 03.01.18.
 */
class Profile {
    lateinit var nick: String
    lateinit var avatarUrl: String
    lateinit var status: Status
    var lastOnline: String? = null
    var groupColor: String? = null
    lateinit var groupName: String
    var messagesCount: Int = 0
    var interests: String? = null
    var signature: String? = null

    val personalData = mutableListOf<Item>()
    val contacts = mutableListOf<Item>()

    enum class Status { ONLINE, OFFLINE }

    class Item {
        lateinit var name: String
        lateinit var value: String
        var linkUrl: String? = null
        var linkName: String? = null
    }
}