package ru.radiationx.anilibria.presentation

import android.os.Bundle
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.ui.fragments.article.details.ArticleFragment
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFragment
import ru.terrakok.cicerone.Router
import java.util.regex.Pattern

/**
 * Created by radiationx on 03.02.18.
 */
class LinkRouter : LinkHandler {

    private val releaseDetail by lazy {
        Pattern.compile("\\/release\\/([\\s\\S]*?)\\.html")
    }

    private val articleDetail by lazy {
        Pattern.compile("\\/[a-zA-Z0-9]+\\/([a-zA-Z0-9]+)\\/?\$")
    }

    override fun handle(url: String, router: Router): Boolean {
        releaseDetail.matcher(url).let {
            if (it.find()) {
                val args: Bundle = Bundle().apply {
                    putString(ReleaseFragment.ARG_ID_CODE, it.group(1))
                }
                router.navigateTo(Screens.RELEASE_DETAILS, args)
                return true
            }
        }
        articleDetail.matcher(url).let {
            if (it.find()) {
                val args: Bundle = Bundle().apply {
                    putString(ArticleFragment.ARG_ID_NAME, it.group(1))
                }
                router.navigateTo(Screens.ARTICLE_DETAILS, args)
                return true
            }
        }
        return false
    }

}