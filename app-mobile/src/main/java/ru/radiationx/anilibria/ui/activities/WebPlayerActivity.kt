package ru.radiationx.anilibria.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.apptheme.AppTheme
import ru.radiationx.anilibria.databinding.ActivityMoonBinding
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.ui.common.Templates
import ru.radiationx.data.analytics.features.ActivityLaunchAnalytics
import ru.radiationx.data.analytics.features.WebPlayerAnalytics
import ru.radiationx.data.app.config.ApiConfig
import ru.radiationx.data.common.toPathUrl
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import ru.radiationx.shared.ktx.android.WebResourceErrorCompat
import ru.radiationx.shared.ktx.android.WebResourceRequestCompat
import ru.radiationx.shared.ktx.android.WebViewClientCompat
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.isLaunchedFromHistory
import ru.radiationx.shared.ktx.android.setWebViewClientCompat
import ru.radiationx.shared.ktx.android.startMainActivity
import ru.radiationx.shared.ktx.android.toException
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber


class WebPlayerActivity : BaseActivity(R.layout.activity_moon) {

    companion object {
        const val ARG_URL = "iframe_url"
        const val ARG_RELEASE_CODE = "release_code"

        fun newIntent(context: Context, link: String, code: String) =
            Intent(context, WebPlayerActivity::class.java).apply {
                putExtra(ARG_URL, link)
                putExtra(ARG_RELEASE_CODE, code)
            }
    }

    private val argUrl by lazy { getExtraNotNull(ARG_URL, "") }
    private val argReleaseCode by lazy { getExtraNotNull(ARG_RELEASE_CODE, "") }

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(webPlayerAnalytics::useTime)
    }

    private val binding by viewBinding<ActivityMoonBinding>()

    private val apiConfig by inject<ApiConfig>()

    private val systemUtils by inject<SystemUtils>()

    private val webPlayerAnalytics by inject<WebPlayerAnalytics>()

    private fun isInvalidIntent(): Boolean {
        return isLaunchedFromHistory() || argUrl.isEmpty() || argReleaseCode.isEmpty()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isInvalidIntent()) {
            get<ActivityLaunchAnalytics>().launchFromHistory(this, savedInstanceState)
            startMainActivity()
            finish()
            return
        }
        lifecycle.addObserver(useTimeCounter)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()

        binding.webView.settings.apply {
            cacheMode = WebSettings.LOAD_NO_CACHE
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
                    systemUtils.open(url)
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
                onPageError(error.toException())
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequestCompat,
                errorResponse: WebResourceResponse,
            ) {
                if (view.url == request.url.toString()) {
                    onPageError(errorResponse.toException(request))
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequestCompat,
                error: WebResourceErrorCompat,
            ) {
                if (view.url == request.url.toString()) {
                    onPageError(error.toException(request))
                }
            }
        }

        binding.webView.setWebViewClientCompat(webViewClient)

        loadUrl()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isInvalidIntent()) {
            binding.webView.setWebViewClientCompat(null)
        }
    }

    private fun loadUrl() {
        // todo API2 use actual url
        val releaseUrl = "/release/$argReleaseCode.html"
            .toPathUrl()
            .withBase(apiConfig.widget)

        val template = get<Templates>().videoPageTemplate
        template.setVariableOpt("iframe_url", argUrl)

        binding.webView.easyLoadData(releaseUrl, template.generateWithTheme(AppTheme.DARK))
    }

    private fun onPageError(error: Exception) {
        Timber.e(error, "onPageError")
        webPlayerAnalytics.error()
    }
}
