package ru.radiationx.anilibria.ui.fragments

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_antiddos.*

import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.interactors.AntiDdosInteractor

class GoogleCaptchaActivity : FragmentActivity() {
    private var content = ""
    private var contentUrl = ""

    private val antiDdosInteractor = App.injections.antiDdosInteractor

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antiddos)
        if (intent != null) {
            content = intent.getStringExtra("content")
            contentUrl = intent.getStringExtra("url")
        }
        antiddos_title.text = "Google Captcha"
        antiddos_skip.setOnClickListener { finish() }
        antiddos_webview.webViewClient = CaptchaWebViewClient()
        antiddos_webview.settings.javaScriptEnabled = true
        val uri = Uri.parse(contentUrl)
        val domain = "${uri.scheme}://${uri.host}"
        Log.e("GoogleCaptchaActivity", "domain: $domain")
        antiddos_webview.easyLoadData(domain, content)

    }

    private inner class CaptchaWebViewClient : WebViewClient() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            Log.e("GoogleCaptchaActivity", "shouldInterceptRequest 21: " + request.url)
            return super.shouldInterceptRequest(view, request)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            Log.e("GoogleCaptchaActivity", "shouldOverrideUrlLoading 21: " + request.url + " : " + request.method)
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            Log.e("GoogleCaptchaActivity", "shouldOverrideUrlLoading 19: $url")
            finish()
            return false
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        antiDdosInteractor.newCompleteEvent(AntiDdosInteractor.EVENT_GOOGLE_CAPTCHA)
        antiddos_webview.webViewClient = null
        antiddos_webview.stopLoading()
        antiddos_webview.endWork()
    }
}
