package ru.radiationx.anilibria.ui.activities

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.*
import kotlinx.android.synthetic.main.activity_moon.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.di.injectDependencies
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.analytics.features.WebPlayerAnalytics
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.shared.ktx.android.toException
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import java.util.regex.Pattern
import javax.inject.Inject


class WebPlayerActivity : BaseActivity() {

    companion object {
        const val ARG_URL = "iframe_url"
        const val ARG_RELEASE_CODE = "release_code"
    }

    private var argUrl: String = ""
    private var argReleaseCode: String = ""

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(webPlayerAnalytics::useTime)
    }

    @Inject
    lateinit var apiConfig: ApiConfig

    @Inject
    lateinit var webPlayerAnalytics: WebPlayerAnalytics


    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
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
                val matcher =
                    Pattern.compile("https?:\\/\\/(?:vk\\.com\\/video_ext|streamguard\\.cc|kodik\\.info)")
                        .matcher(url)
                return if (matcher.find()) {
                    false
                } else {
                    Utils.externalLink(url.orEmpty())
                    true
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                webPlayerAnalytics.loaded()
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                webPlayerAnalytics.error(error.toException())
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view?.url == request?.url?.toString()) {
                    webPlayerAnalytics.error(errorResponse.toException(request))
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view?.url == request?.url?.toString()) {
                    webPlayerAnalytics.error(error.toException(request))
                }
            }
        }

        loadUrl()
    }

    private fun loadUrl() {
        val releaseUrl = "${apiConfig.widgetsSiteUrl}/release/$argReleaseCode.html\""

        val template = App.instance.videoPageTemplate
        template.setVariableOpt("iframe_url", argUrl)

        webView.easyLoadData(releaseUrl, template.generateWithTheme(AppThemeController.AppTheme.DARK))
    }
}
