package ru.radiationx.anilibria.ui.fragments.auth

import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.regex.Pattern

class AuthPatternWebViewClient(
    private val resultListener: (String) -> Unit
) : WebViewClient() {

    var resultPattern: String? = null

    @Deprecated("Deprecated in Java")
    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        resultPattern?.let { resultPattern ->
            val matchSuccess = try {
                val matcher = Pattern.compile(resultPattern).matcher(url.orEmpty())
                if (matcher.find()) {
                    matcher.group(1) != null
                } else {
                    false
                }
            } catch (ignore: Exception) {
                false
            }
            if (matchSuccess) {
                val result = url.orEmpty()
                resultListener.invoke(result)
                return true
            }
        }
        return super.shouldOverrideUrlLoading(view, url)
    }
}