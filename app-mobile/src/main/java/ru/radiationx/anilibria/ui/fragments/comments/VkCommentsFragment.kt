package ru.radiationx.anilibria.ui.fragments.comments

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_webview.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.getDependency
import ru.radiationx.shared_app.injectDependencies
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.extension.getWebStyleType
import ru.radiationx.anilibria.extension.isDark
import ru.radiationx.anilibria.presentation.comments.VkCommentsPresenter
import ru.radiationx.anilibria.presentation.comments.VkCommentsView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.data.entity.app.page.VkComments
import ru.radiationx.shared.ktx.android.toBase64
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.DI
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

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    private val disposables = CompositeDisposable()

    @InjectPresenter
    lateinit var presenter: VkCommentsPresenter

    @ProvidePresenter
    fun providePresenter(): VkCommentsPresenter = getDependency(VkCommentsPresenter::class.java, screenScope)

    override fun getBaseLayout(): Int = R.layout.fragment_webview

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

        savedInstanceState?.let {
            webViewScrollPos = it.getInt(WEB_VIEW_SCROLL_Y, 0)
        }

        webView.setJsLifeCycleListener(jsLifeCycleListener)

        webView.addJavascriptInterface(this, "KEK")


        webView.settings.apply {
            setAppCacheEnabled(true)
            this.databaseEnabled = true
            //setSupportMultipleWindows(true)
            //this.javaScriptCanOpenWindowsAutomatically = false
        }

        webView.webViewClient = vkWebViewClient
        webView.webChromeClient = vkWebChromeClient

        val cookieManager = CookieManager.getInstance()

        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }
        /*val template = App.instance.staticPageTemplate
        webView.easyLoadData(Api.SITE_URL, template.generateWithTheme(appThemeHolder.getTheme()))*/

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
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroyView() {
        webView.endWork()
        super.onDestroyView()
    }

    override fun setRefreshing(refreshing: Boolean) {
        progressBarWv.visible(refreshing)
    }

    override fun showBody(comments: VkComments) {
        if (webView.url != comments.baseUrl) {
            val template = App.instance.vkCommentsTemplate
            webView.easyLoadData(comments.baseUrl, template.generateWithTheme(appThemeHolder.getTheme()))
        }
        webView?.evalJs("ViewModel.setText('content','${comments.script.toBase64()}');")
        webView?.postDelayed({
            webView?.evalJs("KEK.log('<!DOCTYPE html><html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')")

        }, 5000)
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
        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
            Log.d("kukosina", "onCreateWindow $isDialog, $isUserGesture")
            val newWebView = WebView(context)
            AlertDialog.Builder(context!!)
                    .setView(newWebView)
                    .show()
            //addView(newWebView)
            val transport = resultMsg.obj as WebView.WebViewTransport
            transport.webView = newWebView
            resultMsg.sendToTarget()
            return true
        }

    }

    private val vkWebViewClient = object : WebViewClient() {

        var loadingFinished = true
        var redirect = false

        private val authRequestRegex = Regex("oauth\\.vk\\.com\\/authorize\\?|vk\\.com\\/login\\?")

        @Suppress("DEPRECATION", "OverridingDeprecatedMember")
        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
            return if (url?.contains("widget_comments.css") == true) {
                val client = Toothpick.openScopes(DI.DEFAULT_SCOPE, screenScope).getInstance(IClient::class.java, MainClient::class.java.name)

                Log.d("S_DEF_LOG", "CHANGE CSS")
                val cssSrc = try {
                    client.get(url.orEmpty(), emptyMap()).blockingGet()
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    return WebResourceResponse("text/css", "utf-8", ByteArrayInputStream(ex.message.orEmpty().toByteArray(StandardCharsets.UTF_8)))
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
                super.shouldInterceptRequest(view, url)
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
            loadingFinished = false
            //progressBar.visibility = View.VISIBLE

            Log.e("S_DEF_LOG", "ON onPageStarted")
            //SHOW LOADING IF IT ISNT ALREADY VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {

            Log.e("S_DEF_LOG", "ON onPageFinished")
            if (!redirect) {
                loadingFinished = true
            }

            if (loadingFinished && !redirect) {
                //progressBar.visibility = View.GONE
            } else {
                redirect = false
            }
        }

    }

}