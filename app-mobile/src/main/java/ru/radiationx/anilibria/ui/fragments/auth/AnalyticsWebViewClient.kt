package ru.radiationx.anilibria.ui.fragments.auth

import android.net.http.SslError
import android.os.Build
import android.webkit.*
import ru.radiationx.shared.ktx.android.toException

class AnalyticsWebViewClient(
    private val onPageCommitError: (Exception) -> Unit
) : WebViewClient() {

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        super.onReceivedSslError(view, handler, error)
        onPageCommitError.invoke(error.toException())
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view?.url == request?.url?.toString()) {
            onPageCommitError.invoke(errorResponse.toException(request))
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view?.url == request?.url?.toString()) {
            onPageCommitError.invoke(error.toException(request))
        }
    }
}