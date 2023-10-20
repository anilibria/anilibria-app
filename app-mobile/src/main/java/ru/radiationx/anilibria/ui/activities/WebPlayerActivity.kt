package ru.radiationx.anilibria.ui.activities

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.view.WindowManager
import android.webkit.*
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.apptheme.AppTheme
import ru.radiationx.anilibria.databinding.ActivityMoonBinding
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.ui.common.Templates
import ru.radiationx.data.analytics.features.WebPlayerAnalytics
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.WebResourceErrorCompat
import ru.radiationx.shared.ktx.android.WebResourceRequestCompat
import ru.radiationx.shared.ktx.android.WebViewClientCompat
import ru.radiationx.shared.ktx.android.setWebViewClientCompat
import ru.radiationx.shared.ktx.android.toException
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.common.SystemUtils


class WebPlayerActivity : BaseActivity(R.layout.activity_moon) {

    companion object {
        const val ARG_URL = "iframe_url"
        const val ARG_RELEASE_CODE = "release_code"
    }

    private var argUrl: String = ""
    private var argReleaseCode: String = ""

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(webPlayerAnalytics::useTime)
    }

    private val binding by viewBinding<ActivityMoonBinding>()

    private val apiConfig by inject<ApiConfig>()

    private val systemUtils by inject<SystemUtils>()

    private val webPlayerAnalytics by inject<WebPlayerAnalytics>()


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(useTimeCounter)

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
        supportActionBar?.hide()

        binding.webView.settings.apply {
            cacheMode = WebSettings.LOAD_NO_CACHE
            javaScriptEnabled = true
        }

        val webViewClient = object : WebViewClientCompat() {

            private val urlRegex =
                Regex("https?:\\/\\/(?:vk\\.com\\/video_ext|streamguard\\.cc|kodik\\.info)")

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequestCompat,
            ): Boolean {
                val url = request.url.toString()
                return urlRegex.find(url)?.let {
                    systemUtils.externalLink(url)
                    true
                } ?: false
            }

            override fun onPageFinished(view: WebView, url: String) {
                webPlayerAnalytics.loaded()
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError,
            ) {
                webPlayerAnalytics.error(error.toException())
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequestCompat,
                errorResponse: WebResourceResponse,
            ) {
                if (view.url == request.url.toString()) {
                    webPlayerAnalytics.error(errorResponse.toException(request))
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequestCompat,
                error: WebResourceErrorCompat,
            ) {
                if (view.url == request.url.toString()) {
                    webPlayerAnalytics.error(error.toException(request))
                }
            }
        }

        binding.webView.setWebViewClientCompat(webViewClient)

        loadUrl()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.setWebViewClientCompat(null)
    }

    private fun loadUrl() {
        val releaseUrl = "${apiConfig.widgetsSiteUrl}/release/$argReleaseCode.html\""

        val template = get<Templates>().videoPageTemplate
        template.setVariableOpt("iframe_url", argUrl)

        binding.webView.easyLoadData(releaseUrl, template.generateWithTheme(AppTheme.DARK))
    }
}
