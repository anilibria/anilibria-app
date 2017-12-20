package ru.radiationx.anilibria.ui.fragments.other

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import kotlinx.android.synthetic.main.fragment_article.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import android.webkit.WebSettings
import android.os.Build
import android.webkit.WebView
import ru.radiationx.anilibria.ui.fragments.article.ArticleAdapter


/**
 * Created by radiationx on 16.12.17.
 */
class OtherFragment : BaseFragment() {
    override val layoutRes: Int = R.layout.fragment_blank

    override fun onBackPressed(): Boolean {
        return false
    }
}
