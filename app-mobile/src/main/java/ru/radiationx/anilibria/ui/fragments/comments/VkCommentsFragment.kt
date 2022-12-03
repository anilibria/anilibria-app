package ru.radiationx.anilibria.ui.fragments.comments

import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.anilibria.databinding.FragmentVkCommentsBinding
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.extension.getWebStyleType
import ru.radiationx.anilibria.extension.isDark
import ru.radiationx.anilibria.model.loading.hasAnyLoading
import ru.radiationx.anilibria.presentation.comments.VkCommentsViewModel
import ru.radiationx.anilibria.presentation.release.details.ReleaseExtra
import ru.radiationx.anilibria.ui.common.Templates
import ru.radiationx.anilibria.ui.common.webpage.WebPageStateWebViewClient
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.common.webpage.compositeWebViewClientOf
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.quill.quillGet
import ru.radiationx.quill.quillInject
import ru.radiationx.quill.quillViewModel
import ru.radiationx.shared.ktx.android.getExtra
import ru.radiationx.shared.ktx.android.toBase64
import ru.radiationx.shared.ktx.android.toException
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets


class VkCommentsFragment : BaseDimensionsFragment(R.layout.fragment_vk_comments) {

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ID_CODE: String = "release_id_code"
        const val WEB_VIEW_SCROLL_Y = "wvsy"
    }

    private var webViewScrollPos = 0
    private var currentVkCommentsState: VkCommentsState? = null

    private val binding by viewBinding<FragmentVkCommentsBinding>()

    private val viewModel by quillViewModel<VkCommentsViewModel>{
        ReleaseExtra(
            id = getExtra(ARG_ID),
            code = getExtra(ARG_ID_CODE),
            release = null
        )
    }

    private val appThemeController by quillInject<AppThemeController>()

    private val systemUtils by quillInject<SystemUtils>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webErrorView.setPrimaryButtonClickListener {
            viewModel.pageReload()
        }

        binding.vkBlockedErrorView.setSecondaryClickListener {
            viewModel.closeVkBlockedError()
        }

        binding.dataErrorView.setPrimaryButtonClickListener {
            viewModel.refresh()
        }

        binding.jsErrorView.setPrimaryButtonClickListener {
            viewModel.closeJsError()
        }

        savedInstanceState?.let {
            webViewScrollPos = it.getInt(WEB_VIEW_SCROLL_Y, 0)
        }

        binding.webView.setJsLifeCycleListener(jsLifeCycleListener)
        binding.webView.addJavascriptInterface(this, "KEK")

        binding.webView.settings.apply {
            this.databaseEnabled = true
        }

        binding.webView.webViewClient = composite
        binding.webView.webChromeClient = vkWebChromeClient

        val cookieManager = CookieManager.getInstance()

        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(binding.webView, true)
        }

        appThemeController
            .observeTheme()
            .onEach {
                binding.webView.evalJs("changeStyleType(\"${it.getWebStyleType()}\")")
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state.onEach { state ->
            showState(state)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state.mapNotNull { it.data.data }.distinctUntilChanged().onEach {
            showBody(it)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.reloadEvent.onEach {
            binding.webView.reload()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.let {
            outState.putInt(WEB_VIEW_SCROLL_Y, it.scrollY)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.setVisibleToUser(false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.setVisibleToUser(true)
    }

    override fun onDestroyView() {
        binding.webView.endWork()
        super.onDestroyView()
    }

    private fun showState(state: VkCommentsScreenState) {
        val anyLoading = state.data.hasAnyLoading() || state.pageState == WebPageViewState.Loading
        binding.progressBarWv.isVisible = anyLoading

        binding.webView.isVisible = state.pageState == WebPageViewState.Success && !anyLoading
        binding.webErrorView.isVisible = state.pageState is WebPageViewState.Error
        val webErrorDesc = (state.pageState as? WebPageViewState.Error?)?.error?.description
        binding.webErrorView.setSubtitle(webErrorDesc)

        binding.vkBlockedErrorView.isVisible = state.vkBlockedVisible

        binding.dataErrorView.isVisible = state.data.error != null

        binding.jsErrorView.isVisible = state.jsErrorVisible
    }

    private fun showBody(comments: VkCommentsState) {
        if (currentVkCommentsState == comments) {
            return
        }
        currentVkCommentsState = comments

        val template = quillGet<Templates>().vkCommentsTemplate
        binding.webView.easyLoadData(
            comments.url,
            template.generateWithTheme(appThemeController.getTheme())
        )
        binding.webView.evalJs("ViewModel.setText('content','${comments.script.toBase64()}');")
    }

    private val jsLifeCycleListener = object : ExtendedWebView.JsLifeCycleListener {
        override fun onDomContentComplete(actions: ArrayList<String>?) {
        }

        override fun onPageComplete(actions: ArrayList<String>?) {
            binding.webView.syncWithJs {
                binding.webView.scrollTo(0, webViewScrollPos)
            }
        }
    }

    @JavascriptInterface
    fun log(string: String) {
        Log.d("VkCommentsFragment.log", string)
    }

    private val vkWebChromeClient = object : WebChromeClient() {

        private val jsErrorRegex = Regex("Uncaught (?:\\w+)Error:")
        private val sourceRegex = Regex("https?://vk\\.com/")

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            return super.onConsoleMessage(consoleMessage)
        }

        override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
            super.onConsoleMessage(message, lineNumber, sourceID)
            val hasJsError = jsErrorRegex.containsMatchIn(message.orEmpty())
            val isVkSource = sourceRegex.containsMatchIn(sourceID.orEmpty())
            if (hasJsError && isVkSource) {
                viewModel.notifyNewJsError()
            }
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            val newWebView = WebView(requireContext())
            AlertDialog.Builder(requireContext())
                .setView(newWebView)
                .show()
            val transport = resultMsg.obj as WebView.WebViewTransport
            transport.webView = newWebView
            resultMsg.sendToTarget()
            return true
        }
    }

    private val stateClient = WebPageStateWebViewClient {
        viewModel.onNewPageState(it)
    }

    private val vkWebViewClient = object : WebViewClient() {

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
                val networkClient = quillGet<IClient>(MainClient::class)
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

                val commentsCss = quillGet<VkCommentsCss>()
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
            error: SslError?
        ) {
            super.onReceivedSslError(view, handler, error)
            viewModel.onPageCommitError(error.toException())
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view?.url == request?.url?.toString()) {
                viewModel.onPageCommitError(errorResponse.toException(request))
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view?.url == request?.url?.toString()) {
                viewModel.onPageCommitError(error.toException(request))
            }
        }
    }

    private val composite = compositeWebViewClientOf(
        stateClient,
        vkWebViewClient
    )
}