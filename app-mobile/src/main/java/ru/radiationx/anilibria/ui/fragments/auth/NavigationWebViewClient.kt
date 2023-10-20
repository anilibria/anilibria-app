package ru.radiationx.anilibria.ui.fragments.auth

import android.webkit.WebView
import ru.radiationx.shared.ktx.android.WebResourceRequestCompat
import ru.radiationx.shared.ktx.android.WebViewClientCompat

class NavigationWebViewClient(
    private val navigationListener: (String) -> Boolean,
) : WebViewClientCompat() {

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequestCompat,
    ): Boolean {
        val result = navigationListener.invoke(request.url.toString())
        if (result) {
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
}