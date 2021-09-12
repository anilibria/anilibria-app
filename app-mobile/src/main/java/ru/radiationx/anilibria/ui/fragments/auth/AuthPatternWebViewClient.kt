package ru.radiationx.anilibria.ui.fragments.auth

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import ru.radiationx.data.entity.app.auth.SocialAuth
import java.util.regex.Pattern

class AuthPatternWebViewClient(
    private val resultListener: (String) -> Unit
) : WebViewClient() {

    var authData: SocialAuth? = null

    @Suppress("OverridingDeprecatedMember")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        authData?.let { data ->
            val matchSuccess = try {
                val matcher = Pattern.compile(data.resultPattern).matcher(url)
                if (matcher.find()) {
                    matcher.group(1) != null
                } else {
                    false
                }
            } catch (ignore: Exception) {
                false
            }
            Log.d("kekeke", "$matchSuccess, ${data.resultPattern}, $url")
            if (matchSuccess) {
                val result = url.orEmpty()
                resultListener.invoke(result)
                return true
            }
        }
        return super.shouldOverrideUrlLoading(view, url)
    }
}