package ru.radiationx.data.datasource.remote.parsers

import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.app.profile.Profile
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 03.01.18.
 */
class ProfileParser @Inject constructor(
    private val apiUtils: IApiUtils,
    private val apiConfig: ApiConfig
) {

    /*
    * 1.    String  Nick
    * 2.    String  Avatar url
    * 3.    String  Status
    * 4.    String? Last online
    * 5.    String? Group color
    * 6.    String  Group name
    * 7.    Int     Messages count
    * 8.    String  Личные данные
    * 9.    String  Контактная информация
    * 10.   String^ Интересы
    * 11.   String^ Подпись
    * */
    private val mainPatternSrc =
        "<span id=\"profile-nickname\"[^>]*?><b>([\\s\\S]*?)<\\/b><\\/span>[\\s\\S]*?<div id=\"profile-avatar-wrapper\">[^<]*?<img[^>]*?src=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<span id=\"user-status-([^\"]*?)\"[^>]*?>(?:[\\s\\S]*?<span id=\"user-offline-since\"[^>]*?>[^<]*?<b>был в сети ([^<]*?)<)?[\\s\\S]*?<span class=\"profile-left-info\"><b>(?:<font color=\"([^\"]*?)\">)?([^<]*?)(?:<\\/font>)?<\\/b>[\\s\\S]*?<span class=\"profile-left-info\">[^<]*?<font[^>]*?>(\\d+)<\\/font>[\\s\\S]*?<div id=\"profile-info-right-side\">[^<]*?<div class=\"profile-right-block-content\">([\\s\\S]*?)<\\/div>[^<]*?<div class=\"profile-right-block-content\">([\\s\\S]*?)<\\/div>[^<]*?<div class=\"profile-right-block-content last\">[\\s\\S]*?<p class=\"data-label\">[^<]*?<span[^>]*?>([\\s\\S]+?)?<\\/span><\\/p>[\\s\\S]*?<p id=\"user-signature\">([\\s\\S]+?)?<\\/p>[^<]*?<\\/div>"


    /*
    * 1.    String  Name
    * 2.    String  Value
    * 3.    String? Link url
    * 4.    String? Link name
    * */
    private val contentItemPatternSrc =
        "<p class=\"data-label\">([^:]*?):[^<]*?<span class=\"user-data\">((?!Не указано)(?:<a href=\"([^\"]*?)\"[^>]*?>([\\s\\S]*?)<\\/a>|[\\s\\S]*?))<\\/span><\\/p>"

    private val mainPattern: Pattern by lazy {
        Pattern.compile(mainPatternSrc, Pattern.CASE_INSENSITIVE)
    }

    private val contentItemPattern: Pattern by lazy {
        Pattern.compile(contentItemPatternSrc, Pattern.CASE_INSENSITIVE)
    }

    fun profile(httpResponse: String): Profile {
        val result = Profile()

        val matcher: Matcher = mainPattern.matcher(httpResponse)
        if (matcher.find()) {
            result.apply {
                nick = matcher.group(1)
                avatarUrl = "${apiConfig.baseImagesUrl}${matcher.group(2)}"
                status = matcher.group(3).run {
                    if (this.equals(
                            "Online",
                            true
                        )
                    ) Profile.Status.ONLINE else Profile.Status.OFFLINE
                }
                lastOnline = matcher.group(4)
                groupColor = matcher.group(5)
                groupName = matcher.group(6)
                messagesCount = matcher.group(7).toInt()

                parseContentItems(result.personalData, matcher.group(8))
                parseContentItems(result.contacts, matcher.group(9))

                interests = matcher.group(10).run {
                    if (this.isNullOrEmpty()) null else this
                }
                signature = matcher.group(11).run {
                    if (this.isNullOrEmpty()) null else this
                }
            }
        }

        return result
    }

    private fun parseContentItems(items: MutableList<Profile.Item>, source: String) {
        val matcher: Matcher = contentItemPattern.matcher(source)
        while (matcher.find()) {
            items.add(Profile.Item().apply {
                name = matcher.group(1)
                value = apiUtils.escapeHtml(matcher.group(2)).orEmpty()
                linkUrl = matcher.group(3)
                linkName = matcher.group(4)
            })
        }
    }
}