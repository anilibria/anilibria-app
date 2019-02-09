package ru.radiationx.anilibria.ui.common

import ru.radiationx.anilibria.navigation.BaseAppScreen
import ru.radiationx.anilibria.navigation.Screens
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.terrakok.cicerone.Router
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by radiationx on 03.02.18.
 */
class LinkRouter @Inject constructor() : ILinkHandler {

    private val releaseDetail by lazy {
        Pattern.compile("\\/release\\/([\\s\\S]*?)\\.html|tracker\\/\\?ELEMENT_CODE=([^&]+)")
    }

    private val articleDetail by lazy {
        Pattern.compile("\\/[a-zA-Z0-9\\-]+\\/([a-zA-Z0-9\\-]+)\\/?\$")
    }

    override fun handle(url: String, router: Router?, doNavigate: Boolean): Boolean {
        findScreen(url)?.also { screen ->
            if (doNavigate) {
                router?.navigateTo(screen)
            }
            return true
        }
        return false
    }

    override fun findScreen(url: String): BaseAppScreen? {
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