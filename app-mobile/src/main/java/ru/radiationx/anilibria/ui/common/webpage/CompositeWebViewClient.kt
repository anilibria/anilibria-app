package ru.radiationx.anilibria.ui.common.webpage

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.KeyEvent
import android.webkit.*
import androidx.annotation.RequiresApi

fun compositeWebViewClientOf(vararg client: WebViewClient): CompositeWebViewClient {
    return CompositeWebViewClient(client.toList())
}

class CompositeWebViewClient(
    initialClients: List<WebViewClient> = emptyList()
) : WebViewClient() {

    private val clients = mutableListOf(*initialClients.toTypedArray())

    fun registerClient(client: WebViewClient) {
        if (!clients.contains(client)) {
            clients.add(client)
        }
    }

    fun unregisterClient(client: WebViewClient) {
        clients.remove(client)
    }

    fun clear() {
        clients.clear()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return clients.mapFirstBooleanOrNull { it.shouldOverrideUrlLoading(view, url) }
            ?: super.shouldOverrideUrlLoading(view, url)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return clients.mapFirstBooleanOrNull { it.shouldOverrideUrlLoading(view, request) }
            ?: super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        clients.forEach { it.onPageStarted(view, url, favicon) }
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        clients.forEach { it.onPageFinished(view, url) }
        super.onPageFinished(view, url)
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        clients.forEach { it.onLoadResource(view, url) }
        super.onLoadResource(view, url)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPageCommitVisible(view: WebView?, url: String?) {
        clients.forEach { it.onPageCommitVisible(view, url) }
        super.onPageCommitVisible(view, url)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        return clients.mapFirstNotNullOrNull { it.shouldInterceptRequest(view, url) }
            ?: super.shouldInterceptRequest(view, url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return clients.mapFirstNotNullOrNull { it.shouldInterceptRequest(view, request) }
            ?: super.shouldInterceptRequest(view, request)
    }

    @Deprecated("Deprecated in Java")
    override fun onTooManyRedirects(view: WebView?, cancelMsg: Message?, continueMsg: Message?) {
        clients.forEach { it.onTooManyRedirects(view, cancelMsg, continueMsg) }
        super.onTooManyRedirects(view, cancelMsg, continueMsg)
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        clients.forEach { it.onReceivedError(view, errorCode, description, failingUrl) }
        super.onReceivedError(view, errorCode, description, failingUrl)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        clients.forEach { it.onReceivedError(view, request, error) }
        super.onReceivedError(view, request, error)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        clients.forEach { it.onReceivedHttpError(view, request, errorResponse) }
        super.onReceivedHttpError(view, request, errorResponse)
    }

    // TODO fix java.lang.IllegalStateException: { when=0 what=2 target=xb } This message is already in use.
    override fun onFormResubmission(view: WebView?, dontResend: Message?, resend: Message?) {
        //clients.forEach { it.onFormResubmission(view, dontResend, resend) }
        super.onFormResubmission(view, dontResend, resend)
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        clients.forEach { it.doUpdateVisitedHistory(view, url, isReload) }
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        clients.forEach { it.onReceivedSslError(view, handler, error) }
        super.onReceivedSslError(view, handler, error)
    }

    override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
        clients.forEach { it.onReceivedClientCertRequest(view, request) }
        super.onReceivedClientCertRequest(view, request)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        clients.forEach { it.onReceivedHttpAuthRequest(view, handler, host, realm) }
        super.onReceivedHttpAuthRequest(view, handler, host, realm)
    }

    override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        return clients.mapFirstBooleanOrNull { it.shouldOverrideKeyEvent(view, event) }
            ?: super.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
        clients.forEach { it.onUnhandledKeyEvent(view, event) }
        super.onUnhandledKeyEvent(view, event)
    }

    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
        clients.forEach { it.onScaleChanged(view, oldScale, newScale) }
        super.onScaleChanged(view, oldScale, newScale)
    }

    override fun onReceivedLoginRequest(
        view: WebView?,
        realm: String?,
        account: String?,
        args: String?
    ) {
        clients.forEach { it.onReceivedLoginRequest(view, realm, account, args) }
        super.onReceivedLoginRequest(view, realm, account, args)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        return clients.mapFirstBooleanOrNull { it.onRenderProcessGone(view, detail) }
            ?: super.onRenderProcessGone(view, detail)
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onSafeBrowsingHit(
        view: WebView?,
        request: WebResourceRequest?,
        threatType: Int,
        callback: SafeBrowsingResponse?
    ) {
        clients.forEach { it.onSafeBrowsingHit(view, request, threatType, callback) }
        super.onSafeBrowsingHit(view, request, threatType, callback)
    }

    private fun <T> List<T>.mapFirstBooleanOrNull(block: (T) -> Boolean): Boolean? {
        for (element in this) {
            if (block(element)) return true
        }
        return null
    }

    private fun <T, R> List<T>.mapFirstNotNullOrNull(block: (T) -> R?): R? {
        for (element in this) {
            val result = block(element)
            if (result != null) return result
        }
        return null
    }

}