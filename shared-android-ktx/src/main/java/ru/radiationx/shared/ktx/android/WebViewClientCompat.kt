package ru.radiationx.shared.ktx.android

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi


fun WebView.setWebViewClientCompat(clientCompat: WebViewClientCompat?) {
    webViewClient = clientCompat?.webViewClient ?: WebViewClient()
}

open class WebViewClientCompat {

    val webViewClient: WebViewClient by lazy {
        DefaultWebviewClient(this)
    }

    /* Normal methods */

    open fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
    }

    open fun onPageFinished(view: WebView, url: String) {
    }

    open fun onPageCommitVisible(view: WebView, url: String) {
    }

    open fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: SslError,
    ) {
    }

    /* Compat methods */
    open fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequestCompat): Boolean {
        return false
    }

    open fun onReceivedError(
        view: WebView,
        request: WebResourceRequestCompat,
        error: WebResourceErrorCompat,
    ) {
    }

    open fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequestCompat,
        errorResponse: WebResourceResponse,
    ) {
    }

    open fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequestCompat,
    ): WebResourceResponse? {
        return null
    }
}

private class DefaultWebviewClient(
    private val compat: WebViewClientCompat,
) : WebViewClient() {

    /* Normal methods */

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        compat.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        compat.onPageFinished(view, url)
    }

    override fun onPageCommitVisible(view: WebView, url: String) {
        compat.onPageCommitVisible(view, url)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        compat.onReceivedSslError(view, handler, error)
    }

    /* Compat methods */
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean {
        return compat.shouldOverrideUrlLoading(view, request.toCompat())
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return compat.shouldOverrideUrlLoading(view, url.asWebResourceRequest())
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String,
    ) {
        compat.onReceivedError(
            view,
            failingUrl.asWebResourceRequest(),
            WebResourceErrorCompat(errorCode, description)
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        compat.onReceivedError(view, request.toCompat(), error.toCompat())
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse,
    ) {
        compat.onReceivedHttpError(view, request.toCompat(), errorResponse)
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        return compat.shouldInterceptRequest(view, request.toCompat())
    }

    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        return compat.shouldInterceptRequest(view, url.asWebResourceRequest())
    }
}

data class WebResourceRequestCompat(
    val url: Uri,
    val isForMainFrame: Boolean = true,
    val isRedirect: Boolean = false,
    val hasGesture: Boolean = false,
    val method: String = "GET",
    val requestHeaders: Map<String, String>? = null,
)

data class WebResourceErrorCompat(
    val errorCode: Int,
    val description: CharSequence,
)

@RequiresApi(Build.VERSION_CODES.M)
private fun WebResourceError.toCompat(): WebResourceErrorCompat {
    return WebResourceErrorCompat(
        errorCode = errorCode,
        description = description
    )
}

private fun String.asWebResourceRequest(): WebResourceRequestCompat {
    return WebResourceRequestCompat(Uri.parse(this))
}

private fun WebResourceRequest.toCompat(): WebResourceRequestCompat {
    val isRedirectCompat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        isRedirect
    } else {
        false
    }
    return WebResourceRequestCompat(
        url = url,
        isForMainFrame = isForMainFrame,
        isRedirect = isRedirectCompat,
        hasGesture = hasGesture(),
        method = method,
        requestHeaders = requestHeaders
    )
}