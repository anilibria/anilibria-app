package ru.radiationx.anilibria.ui.common.webpage

import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi

@Suppress("DEPRECATION")
class WebPageStateWebViewClient(
    listener: (WebPageViewState) -> Unit
) : WebViewClient() {

    private val stateHandler = WebPageStateHandler(listener)

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        stateHandler.onLoadingChanged(true)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        stateHandler.onLoadingChanged(false)
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        stateHandler.onLoadingChanged(false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (request?.isForMainFrame == true) {
                val webPageError = WebPageError(
                    errorCode = error?.errorCode ?: -1,
                    description = error?.description?.toString().orEmpty(),
                    url = request.url?.toString().orEmpty()
                )
                stateHandler.onError(webPageError)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            val webPageError = WebPageError(
                errorCode = errorCode,
                description = description.orEmpty(),
                url = failingUrl.orEmpty()
            )
            stateHandler.onError(webPageError)
        }
    }
}