package ru.radiationx.anilibria.ui.fragments.comments.webview

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.webkit.*
import kotlinx.coroutines.runBlocking
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.anilibria.extension.isDark
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsCss
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsViewModel
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.shared.ktx.android.toException
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class VkWebViewClient(
    private val viewModel: VkCommentsViewModel,
    private val systemUtils: SystemUtils,
    private val networkClient: IClient,
    private val commentsCss: VkCommentsCss,
    private val appThemeController: AppThemeController,
) : WebViewClient() {

    var loadingFinished = true
    var redirect = false
    var authCheckIntercepted = false

    private val authRequestRegex = Regex("oauth\\.vk\\.com\\/authorize\\?|vk\\.com\\/login\\?")
    private val authCheckRegex = Regex("vk\\.com\\/login\\?act=authcheck")
    private val commentsRegex = Regex("widget_comments(?:\\.\\w+?)?\\.css")

    @Suppress("DEPRECATION", "OverridingDeprecatedMember")
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        return tryInterceptAuthCheck(view, url)
            ?: tryInterceptComments(view, url)
            ?: super.shouldInterceptRequest(view, url)
    }

    private fun tryInterceptAuthCheck(view: WebView?, url: String?): WebResourceResponse? {
        val needAuth = authCheckRegex.containsMatchIn(url.orEmpty())
        if (needAuth && !authCheckIntercepted) {
            authCheckIntercepted = true
            val mobileUrl = if (!url.orEmpty().contains("/m.vk.com/")) {
                url.orEmpty().replace("vk.com", "/m.vk.com/")
            } else {
                url.orEmpty()
            }
            viewModel.authRequest(mobileUrl)
        }
        return null
    }

    private fun tryInterceptComments(view: WebView?, url: String?): WebResourceResponse? {
        val needIntercept = commentsRegex.containsMatchIn(url.orEmpty())
        return if (needIntercept) {
            val cssSrc = try {
                runBlocking { networkClient.get(url.orEmpty(), emptyMap()) }
            } catch (ex: Throwable) {
                Timber.e(ex)
                return WebResourceResponse(
                    "text/css",
                    "utf-8",
                    ByteArrayInputStream(
                        ex.message.orEmpty().toByteArray(StandardCharsets.UTF_8)
                    )
                )
            }
            var newCss = cssSrc

            val fixCss = if (appThemeController.getTheme().isDark()) {
                commentsCss.dark
            } else {
                commentsCss.light
            }

            newCss += fixCss

            val newData = ByteArrayInputStream(newCss.toByteArray(StandardCharsets.UTF_8))
            WebResourceResponse("text/css", "utf-8", newData)
        } else {
            null
        }
    }

    @Suppress("OverridingDeprecatedMember")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val cookies = CookieManager.getInstance().getCookie(url)
        if (!loadingFinished) {
            redirect = true
        }

        loadingFinished = false

        if (url.orEmpty().contains(authRequestRegex)) {
            viewModel.authRequest(url.orEmpty())
            return true
        }
        systemUtils.externalLink(url.orEmpty())
        return true
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        authCheckIntercepted = false
        loadingFinished = false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        if (!redirect) {
            loadingFinished = true
        }

        if (loadingFinished && !redirect) {
            //progressBar.visibility = View.GONE
            viewModel.onPageLoaded()
        } else {
            redirect = false
        }
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?,
    ) {
        super.onReceivedSslError(view, handler, error)
        viewModel.onPageCommitError(error.toException())
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?,
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view?.url == request?.url?.toString()) {
            viewModel.onPageCommitError(errorResponse.toException(request))
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view?.url == request?.url?.toString()) {
            viewModel.onPageCommitError(error.toException(request))
        }
    }
}