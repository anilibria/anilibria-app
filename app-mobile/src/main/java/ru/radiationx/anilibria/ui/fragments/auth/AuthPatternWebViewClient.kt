package ru.radiationx.anilibria.ui.fragments.auth

import android.webkit.WebView
import ru.radiationx.shared.ktx.android.WebResourceRequestCompat
import ru.radiationx.shared.ktx.android.WebViewClientCompat
import java.util.regex.Pattern

class AuthPatternWebViewClient(
    private val resultListener: (String) -> Unit,
) : WebViewClientCompat() {

    var resultPattern: String? = null

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequestCompat,
    ): Boolean {
        resultPattern?.let { resultPattern ->
            val url = request.url.toString()
            val matchSuccess = try {
                val matcher = Pattern.compile(resultPattern).matcher(url)
                if (matcher.find()) {
                    matcher.group(1) != null
                } else {
                    false
                }
            } catch (ignore: Exception) {
                false
            }
            if (matchSuccess) {
                resultListener.invoke(url)
                return true
            }
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
}