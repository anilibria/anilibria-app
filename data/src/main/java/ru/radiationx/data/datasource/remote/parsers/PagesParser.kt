package ru.radiationx.data.datasource.remote.parsers

import org.json.JSONObject
import ru.radiationx.data.datasource.remote.IApiUtils
import ru.radiationx.data.entity.app.page.PageLibria
import ru.radiationx.data.entity.app.page.VkComments
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PagesParser @Inject constructor(
    private val apiUtils: IApiUtils
) {

    private val pagePatternSource =
        "(<div[^>]*?class=\"[^\"]*?news-body[^\"]*?\"[^>]*?>[\\s\\S]*?<\\/div>)[^<]*?<div[^>]*?(?:id=\"vk_comments|class=\"[^\"]*?side[^\"]*?\")"
    private val titlePatternSource = "<title>([\\s\\S]*?)<\\/title>"

    private val pagePattern: Pattern by lazy {
        Pattern.compile(pagePatternSource, Pattern.CASE_INSENSITIVE)
    }

    private val titlePattern: Pattern by lazy {
        Pattern.compile(titlePatternSource, Pattern.CASE_INSENSITIVE)
    }

    fun baseParse(httpResponse: String): PageLibria {
        var matcher = pagePattern.matcher(httpResponse)
        var title = ""
        var content = ""
        while (matcher.find()) {
            content += matcher.group(1)
        }
        matcher = titlePattern.matcher(httpResponse)
        if (matcher.find()) {
            title = matcher.group(1)
        }
        return PageLibria(
            title = title,
            content = content
        )
    }

    fun parseVkComments(jsonResponse: JSONObject): VkComments {
        return VkComments(
            jsonResponse.getString("baseUrl"),
            jsonResponse.getString("script")
        )
    }
}