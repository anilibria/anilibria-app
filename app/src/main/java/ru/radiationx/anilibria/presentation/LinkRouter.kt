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
                val args: Bundle = Bundle().apply {
                    it.group(1)?.let {
                        putString(ReleaseFragment.ARG_ID_CODE, it)
                    }
                    it.group(2)?.let {
                        putString(ReleaseFragment.ARG_ID_CODE, it)
                    }
                }
                return Screens.ReleaseDetails(args)
            }
        }
        articleDetail.matcher(url).let {
            if (it.find()) {
                val args: Bundle = Bundle().apply {
                    putString(ArticleFragment.ARG_ID_NAME, it.group(1))
                }
                return Screens.ArticleDetails(args)
            }
        }
        return null
    }

    private fun checkUnsupported(url: String): Boolean {
        if (url.contains("communication/forum")) return true
        return false
    }

}