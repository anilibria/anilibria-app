package ru.radiationx.anilibria.ui.fragments.auth

import android.webkit.WebView
import android.webkit.WebViewClient

class NavigationWebViewClient(
    private val navigationListener: (String) -> Boolean
) : WebViewClient() {

    @Suppress("OverridingDeprecatedMember")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val result = navigationListener.invoke(url.orEmpty())
        if (result) {
            return true
        }
        return super.shouldOverrideUrlLoading(view, url)
    }
}