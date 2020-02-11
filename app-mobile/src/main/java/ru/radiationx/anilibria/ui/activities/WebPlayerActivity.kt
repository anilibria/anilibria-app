package ru.radiationx.anilibria.ui.activities

import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_moon.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.injectDependencies
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.datasource.remote.address.ApiConfig
import java.util.regex.Pattern
import javax.inject.Inject


class WebPlayerActivity : BaseActivity() {

    companion object {
        const val ARG_URL = "iframe_url"
        const val ARG_RELEASE_CODE = "release_code"
    }

    private var argUrl: String = ""
    private var argReleaseCode: String = ""

    @Inject
    lateinit var apiConfig: ApiConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)

        argUrl = intent?.getStringExtra(ARG_URL).orEmpty()
        argReleaseCode = intent?.getStringExtra(ARG_RELEASE_CODE).orEmpty()

        if (argUrl.isEmpty()) {
            finish()
            return
        }

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_moon)
        supportActionBar?.hide()

        webView.settings.apply {
            setAppCacheEnabled(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
            javaScriptEnabled = true
        }
        webView.webViewClient = object : WebViewClient() {
            @Suppress("OverridingDeprecatedMember")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val matcher = Pattern.compile("https?:\\/\\/(?:vk\\.com\\/video_ext|streamguard\\.cc|kodik\\.info)").matcher(url)
                return if (matcher.find()) {
                    false
                } else {
                    Utils.externalLink(url.orEmpty())
                    true
                }
            }
        }

        loadUrl()
    }

    private fun loadUrl() {
        val releaseUrl = "${apiConfig.widgetsSiteUrl}/release/$argReleaseCode.html\""

        val template = App.instance.videoPageTemplate
        template.setVariableOpt("iframe_url", argUrl)

        webView.easyLoadData(releaseUrl, template.generateWithTheme(AppThemeHolder.AppTheme.DARK))
    }
}
