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
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_vk_comments.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.extension.getWebStyleType
import ru.radiationx.anilibria.extension.isDark
import ru.radiationx.anilibria.model.loading.hasAnyLoading
import ru.radiationx.anilibria.presentation.comments.VkCommentsPresenter
import ru.radiationx.anilibria.presentation.comments.VkCommentsView
import ru.radiationx.anilibria.ui.common.webpage.WebPageStateWebViewClient
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.common.webpage.compositeWebViewClientOf
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.shared.ktx.android.toBase64
import ru.radiationx.shared.ktx.android.toException
import ru.radiationx.shared_app.di.DI
import ru.radiationx.shared_app.di.injectDependencies
import toothpick.Toothpick
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject


class VkCommentsFragment : BaseFragment(), VkCommentsView {

    companion object {
        const val ARG_ID: String = "release_id"
        const val ARG_ID_CODE: String = "release_id_code"
        const val WEB_VIEW_SCROLL_Y = "wvsy"
    }

    private var webViewScrollPos = 0
    private var currentVkCommentsState: VkCommentsState? = null

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    private val disposables = CompositeDisposable()

    @InjectPresenter
    lateinit var presenter: VkCommentsPresenter

    @ProvidePresenter
    fun providePresenter(): VkCommentsPresenter =
        getDependency(VkCommentsPresenter::class.java, screenScope)

    override fun getBaseLayout(): Int = R.layout.fragment_vk_comments

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.also { bundle ->
            presenter.releaseId = bundle.getInt(ARG_ID, presenter.releaseId)
            presenter.releaseIdCode = bundle.getString(ARG_ID_CODE, presenter.releaseIdCode)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webErrorView.setPrimaryButtonClickListener {
            presenter.pageReload()
        }

        dataErrorView.setPrimaryButtonClickListener {
            presenter.refresh()
        }

        jsErrorView.setPrimaryButtonClickListener {
            presenter.closeJsError()
        }

        savedInstanceState?.let {
            webViewScrollPos = it.getInt(WEB_VIEW_SCROLL_Y, 0)
        }

        webView.setJsLifeCycleListener(jsLifeCycleListener)
        webView.addJavascriptInterface(this, "KEK")

        webView.settings.apply {
            setAppCacheEnabled(true)
            this.databaseEnabled = true
        }

        webView.webViewClient = composite
        webView.webChromeClient = vkWebChromeClient

        val cookieManager = CookieManager.getInstance()

        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        disposables.add(
            appThemeHolder
                .observeTheme()
                .subscribe {
                    webView?.evalJs("changeStyleType(\"${it.getWebStyleType()}\")")
                }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView?.let {
            outState.putInt(WEB_VIEW_SCROLL_Y, it.scrollY)
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.setVisibleToUser(false)
    }

    override fun onResume() {
        super.onResume()
        presenter.setVisibleToUser(true)
    }

    override fun onDestroyView() {
        webView.endWork()
        super.onDestroyView()
    }

    override fun pageReloadAction() {
        Log.d("kekeke", "pageReloadAction")
        webView.reload()
    }

    override fun showState(state: VkCommentsScreenState) {
        Log.d("kekeke", "show state $state")
        val anyLoading = state.data.hasAnyLoading() || state.pageState == WebPageViewState.Loading
        progressBarWv.isVisible = anyLoading

        webView.isVisible = state.pageState == WebPageViewState.Success && !anyLoading
        webErrorView.isVisible = state.pageState is WebPageViewState.Error

        dataErrorView.isVisible = state.data.error != null

        jsErrorView.isVisible = state.jsErrorVisible
        state.data.data?.let { showBody(it) }
    }

    private fun showBody(comments: VkCommentsState) {
        if (currentVkCommentsState == comments) {
            return
        }
        currentVkCommentsState = comments

        val template = App.instance.vkCommentsTemplate
        webView.easyLoadData(
            comments.url,
            template.generateWithTheme(appThemeHolder.getTheme())
        )
        webView?.evalJs("ViewModel.setText('content','${comments.script.toBase64()}');")

        // Uncomment for check generated html
        /*webView?.postDelayed({
            webView?.evalJs("KEK.log('<!DOCTYPE html><html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')")
        }, 5000)*/
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    private val jsLifeCycleListener = object : ExtendedWebView.JsLifeCycleListener {
        override fun onDomContentComplete(actions: ArrayList<String>?) {
        }

        override fun onPageComplete(actions: ArrayList<String>?) {
            webView?.syncWithJs {
                webView?.scrollTo(0, webViewScrollPos)
            }
        }
    }

    @JavascriptInterface
    fun log(string: String) {
        Log.d("kekbody", string)
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
                presenter.notifyNewJsError()
            }
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            val newWebView = WebView(context)
            AlertDialog.Builder(context!!)
                .setView(newWebView)
                .show()
            val transport = resultMsg.obj as WebView.WebViewTransport
            transport.webView = newWebView
            resultMsg.sendToTarget()
            return true
        }
    }

    private val stateClient = WebPageStateWebViewClient {
        presenter.onNewPageState(it)
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
                Log.d("kekeke", "tryInterceptAuthCheck $url -> $mobileUrl")
                presenter.authRequest(mobileUrl)
            }
            return null
        }

        private fun tryInterceptComments(view: WebView?, url: String?): WebResourceResponse? {
            val needIntercept = commentsRegex.containsMatchIn(url.orEmpty())
            return if (needIntercept) {
                Log.d("kekeke", "tryInterceptComments $url")
                val client = Toothpick.openScopes(DI.DEFAULT_SCOPE, screenScope)
                    .getInstance(IClient::class.java, MainClient::class.java.name)

                Log.d("S_DEF_LOG", "CHANGE CSS")
                val cssSrc = try {
                    client.get(url.orEmpty(), emptyMap()).blockingGet()
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    return WebResourceResponse(
                        "text/css",
                        "utf-8",
                        ByteArrayInputStream(
                            ex.message.orEmpty().toByteArray(StandardCharsets.UTF_8)
                        )
                    )
                }
                var newCss = cssSrc

                val fixCss = if (appThemeHolder.getTheme().isDark()) {
                    App.instance.vkCommentCssFixDark
                } else {
                    App.instance.vkCommentCssFixLight
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
            Log.e("S_DEF_LOG", "OverrideUrlLoading: $url")
            val cookies = CookieManager.getInstance().getCookie(url)
            Log.d("S_DEF_LOG", "URL COOKIES: '$cookies'")
            if (!loadingFinished) {
                redirect = true
            }

            loadingFinished = false

            if (url.orEmpty().contains(authRequestRegex)) {
                presenter.authRequest(url.orEmpty())
                return true
            }
            Utils.externalLink(url.orEmpty())
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            authCheckIntercepted = false


            loadingFinished = false
            //progressBar.visibility = View.VISIBLE

            Log.e("S_DEF_LOG", "ON onPageStarted")
            //SHOW LOADING IF IT ISNT ALREADY VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            Log.e("S_DEF_LOG", "ON onPageFinished")
            if (!redirect) {
                loadingFinished = true
            }

            if (loadingFinished && !redirect) {
                //progressBar.visibility = View.GONE
                presenter.onPageLoaded()
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
            presenter.onPageCommitError(error.toException())
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view?.url == request?.url?.toString()) {
                presenter.onPageCommitError(errorResponse.toException(request))
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view?.url == request?.url?.toString()) {
                presenter.onPageCommitError(error.toException(request))
            }
        }
    }

    private val composite = compositeWebViewClientOf(
        stateClient,
        vkWebViewClient
    )
}