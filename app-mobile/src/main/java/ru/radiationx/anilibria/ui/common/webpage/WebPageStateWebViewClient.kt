package ru.radiationx.anilibria.ui.common.webpage

import android.graphics.Bitmap
import android.webkit.WebView
import ru.radiationx.shared.ktx.android.WebResourceErrorCompat
import ru.radiationx.shared.ktx.android.WebResourceRequestCompat
import ru.radiationx.shared.ktx.android.WebViewClientCompat

class WebPageStateWebViewClient(
    listener: (WebPageViewState) -> Unit,
) : WebViewClientCompat() {

    private val stateHandler = WebPageStateHandler(listener)

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        stateHandler.onLoadingChanged(true)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        stateHandler.onLoadingChanged(false)
    }

    override fun onPageCommitVisible(view: WebView, url: String) {
        super.onPageCommitVisible(view, url)
        stateHandler.onLoadingChanged(false)
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequestCompat,
        error: WebResourceErrorCompat,
    ) {
        if (request.isForMainFrame) {
            val webPageError = WebPageError(
                errorCode = error.errorCode,
                description = error.description.toString(),
                url = request.url.toString()
            )
            stateHandler.onError(webPageError)
        }
    }
}