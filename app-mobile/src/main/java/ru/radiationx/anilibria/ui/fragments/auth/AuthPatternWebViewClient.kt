package ru.radiationx.anilibria.ui.fragments.auth

import android.webkit.WebView
import ru.radiationx.shared.ktx.android.WebResourceRequestCompat
import ru.radiationx.shared.ktx.android.WebViewClientCompat

class AuthPatternWebViewClient(
    private val resultListener: (String) -> Unit,
) : WebViewClientCompat() {

    var redirectUrl: String? = null

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequestCompat,
    ): Boolean {
        redirectUrl?.let { redirectUrl ->
            val url = request.url.toString()
            val matchSuccess = url.startsWith(redirectUrl)
            if (matchSuccess) {
                resultListener.invoke(url)
                return true
            }
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
}