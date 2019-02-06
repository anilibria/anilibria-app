package ru.radiationx.anilibria.presentation

import android.os.Bundle
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.ui.fragments.article.details.ArticleFragment
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.radiationx.anilibria.ui.navigation.AppRouter
import java.util.regex.Pattern

/**
 * Created by radiationx on 03.02.18.
 */
class LinkRouter : LinkHandler {

    private val releaseDetail by lazy {
        Pattern.compile("\\/release\\/([\\s\\S]*?)\\.html|tracker\\/\\?ELEMENT_CODE=([^&]+)")
    }

    private val articleDetail by lazy {
        Pattern.compile("\\/[a-zA-Z0-9\\-]+\\/([a-zA-Z0-9\\-]+)\\/?\$")
    }

    override fun handle(url: String, router: AppRouter?, doNavigate: Boolean): Boolean {
        findScreen(url)?.also { screen ->
            if (doNavigate) {
                router?.navigateTo(screen)
            }
            return true
        }
        return false
    }

    override fun findScreen(url: String): Screens.AppScreen? {
        if (checkUnsupported(url)) {
            return null
        }
        releaseDetail.matcher(url).let {
            if (it.find()) {
                val code = it.group(1) ?: it.group(2)
                return Screens.ReleaseDetails(code = code)
            }
        }
        articleDetail.matcher(url).let {
            if (it.find()) {
                return Screens.ArticleDetails(it.group(1))
            }
        }
        return null
    }

    private fun checkUnsupported(url: String): Boolean {
        if (url.contains("communication/forum")) return true
        return false
    }

}