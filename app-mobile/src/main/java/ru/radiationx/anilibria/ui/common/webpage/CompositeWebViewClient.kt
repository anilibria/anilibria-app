package ru.radiationx.anilibria.ui.common.webpage

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceResponse
import android.webkit.WebView
import ru.radiationx.shared.ktx.android.WebResourceErrorCompat
import ru.radiationx.shared.ktx.android.WebResourceRequestCompat
import ru.radiationx.shared.ktx.android.WebViewClientCompat

fun compositeWebViewClientOf(vararg client: WebViewClientCompat): CompositeWebViewClient {
    return CompositeWebViewClient(client.toList())
}

class CompositeWebViewClient(
    initialClients: List<WebViewClientCompat> = emptyList(),
) : WebViewClientCompat() {

    private val clients = mutableListOf(*initialClients.toTypedArray())

    fun registerClient(client: WebViewClientCompat) {
        if (!clients.contains(client)) {
            clients.add(client)
        }
    }

    fun unregisterClient(client: WebViewClientCompat) {
        clients.remove(client)
    }

    fun clear() {
        clients.clear()
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        clients.forEach { it.onPageStarted(view, url, favicon) }
    }

    override fun onPageFinished(view: WebView, url: String) {
        clients.forEach { it.onPageFinished(view, url) }
    }

    override fun onPageCommitVisible(view: WebView, url: String) {
        clients.forEach { it.onPageCommitVisible(view, url) }
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        clients.forEach { it.onReceivedSslError(view, handler, error) }
    }

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequestCompat,
    ): Boolean {
        return clients
            .mapFirstBooleanOrNull { it.shouldOverrideUrlLoading(view, request) }
            ?: super.shouldOverrideUrlLoading(view, request)
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequestCompat,
        error: WebResourceErrorCompat,
    ) {
        clients.forEach { it.onReceivedError(view, request, error) }
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequestCompat,
        errorResponse: WebResourceResponse,
    ) {
        clients.forEach { it.onReceivedHttpError(view, request, errorResponse) }
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequestCompat,
    ): WebResourceResponse? {
        return clients
            .mapFirstNotNullOrNull { it.shouldInterceptRequest(view, request) }
            ?: super.shouldInterceptRequest(view, request)
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