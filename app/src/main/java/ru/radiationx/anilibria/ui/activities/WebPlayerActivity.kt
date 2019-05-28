package ru.radiationx.anilibria.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_moon.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.utils.Utils
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject


class WebPlayerActivity : BaseActivity() {

    companion object {
        const val ARG_URL = "iframe_url"
    }

    @Inject
    lateinit var apiConfig: ApiConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_moon)
        supportActionBar?.hide()

        webView.settings.apply {
            setAppCacheEnabled(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
            javaScriptEnabled = true
        }
        intent?.let {
            it.getStringExtra(ARG_URL)?.let { argUrl ->
                val extraHeaders = HashMap<String, String>()
                extraHeaders["Referer"] = apiConfig.widgetsSiteUrl
                Log.e("lalala", "load url $argUrl")
                webView.loadUrl(argUrl, extraHeaders)
                webView.webViewClient = object : WebViewClient() {
                    @Suppress("OverridingDeprecatedMember")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        val matcher = Pattern.compile("https?:\\/\\/(?:vk\\.com\\/video_ext|streamguard\\.cc)").matcher(url)
                        return if (matcher.find()) {
                            false
                        } else {
                            Utils.externalLink(url.orEmpty())
                            true
                        }
                    }
                }
            } ?: finish()
        } ?: finish()
    }

}
