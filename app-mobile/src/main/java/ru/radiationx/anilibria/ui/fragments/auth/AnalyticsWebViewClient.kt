package ru.radiationx.anilibria.ui.fragments.auth

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceResponse
import android.webkit.WebView
import ru.radiationx.shared.ktx.android.WebResourceErrorCompat
import ru.radiationx.shared.ktx.android.WebResourceRequestCompat
import ru.radiationx.shared.ktx.android.WebViewClientCompat
import ru.radiationx.shared.ktx.android.toException

class AnalyticsWebViewClient(
    private val onPageCommitError: (Exception) -> Unit,
) : WebViewClientCompat() {

    override fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: SslError,
    ) {
        onPageCommitError.invoke(error.toException())
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequestCompat,
        errorResponse: WebResourceResponse,
    ) {
        if (view.url == request.url.toString()) {
            onPageCommitError.invoke(errorResponse.toException(request))
        }
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequestCompat,
        error: WebResourceErrorCompat,
    ) {
        super.onReceivedError(view, request, error)
        if (view.url == request.url.toString()) {
            onPageCommitError.invoke(error.toException(request))
        }
    }
}