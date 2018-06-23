package ru.radiationx.anilibria.ui.activities

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_antiddos.*

import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.interactors.AntiDdosInteractor

class BlazingFastActivity : FragmentActivity() {
    private var content = ""
    private var contentUrl = ""

    private val cookieHolder = App.injections.cookieHolder
    private val antiDdosInteractor = App.injections.antiDdosInteractor


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antiddos)
        antiddos_refreshing.visibility = View.VISIBLE
        antiddos_title.text = "Проверка BlazingFast"
        antiddos_skip.setOnClickListener { finish() }
        antiddos_webview.settings.javaScriptEnabled = true
        antiddos_webview.webViewClient = CaptchaWebViewClient()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?){
        if (intent != null) {
            content = intent.getStringExtra("content")
            contentUrl = intent.getStringExtra("url")
        }
        val uri = Uri.parse(contentUrl)
        val domain = "${uri.scheme}://${uri.host}"
        Log.e("BlazingFastActivity", "domain: $domain")
        antiddos_webview.easyLoadData(domain, "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js\"></script><script>$content</script>")
    }

    private inner class CaptchaWebViewClient : WebViewClient() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            Log.e("BlazingFastActivity", "shouldInterceptRequest 21: " + request.url)
            return super.shouldInterceptRequest(view, request)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            Log.e("BlazingFastActivity", "shouldOverrideUrlLoading 21: " + request.url + " : " + request.method)
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val cookies = CookieManager.getInstance().getCookie(url)
            val cookiesArray = cookies.split(";".toRegex()).dropLastWhile { it.isEmpty() }
            for (cookie in cookiesArray) {
                val cookieObj = cookie.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                if (cookieObj[0].toLowerCase().contains(CookieHolder.BLAZINGFAST_WEB_PROTECT.toLowerCase())) {
                    Log.e("BlazingFastActivity", "putCookie '" + cookieObj[0] + "' : '" + cookieObj[1] + "'")
                    cookieHolder.putCookie(url, cookieObj[0], cookieObj[1])
                }
            }
            finish()
            return false
        }
    }

    override fun onBackPressed() {}

    public override fun onDestroy() {
        super.onDestroy()
        antiDdosInteractor.newCompleteEvent(AntiDdosInteractor.EVENT_BLAZING_FAST)
        antiddos_webview.webViewClient = null
        antiddos_webview.stopLoading()
        antiddos_webview.endWork()
    }

}
