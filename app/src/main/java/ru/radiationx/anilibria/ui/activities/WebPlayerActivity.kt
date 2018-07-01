package ru.radiationx.anilibria.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_moon.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.utils.Utils
import java.util.*
import java.util.regex.Pattern


class WebPlayerActivity : AppCompatActivity() {

    companion object {
        const val ARG_URL = "iframe_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
            it.getStringExtra(ARG_URL)?.let {
                val extraHeaders = HashMap<String, String>()
                extraHeaders["Referer"] = Api.SITE_URL
                Log.e("lalala", "load url $it")
                webView.loadUrl(it, extraHeaders)
                webView.webViewClient = object : WebViewClient() {
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
