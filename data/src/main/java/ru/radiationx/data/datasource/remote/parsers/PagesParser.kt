package ru.radiationx.data.datasource.remote.parsers

import ru.radiationx.data.entity.domain.page.PageLibria
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PagesParser @Inject constructor() {

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
            title = matcher.group(1) ?: title
        }
        return PageLibria(
            title = title,
            content = content
        )
    }

}