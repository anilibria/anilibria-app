package ru.radiationx.anilibria.ui.fragments.auth

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_webview.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.di.injectDependencies
import ru.radiationx.anilibria.presentation.auth.social.AuthSocialPresenter
import ru.radiationx.anilibria.presentation.auth.social.AuthSocialView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.utils.Utils
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.toException
import ru.radiationx.shared.ktx.android.visible
import java.lang.Exception
import javax.inject.Inject


/**
 * Created by radiationx on 31.12.17.
 */
class AuthSocialFragment : BaseFragment(), AuthSocialView {

    companion object {
        private const val ARG_KEY = "key"

        fun newInstance(key: String) = AuthSocialFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_KEY, key)
            }
        }
    }

    @Inject
    lateinit var apiConfig: ApiConfig

    @InjectPresenter
    lateinit var presenter: AuthSocialPresenter

    @ProvidePresenter
    fun providePresenter(): AuthSocialPresenter =
        getDependency(AuthSocialPresenter::class.java, screenScope)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            presenter.argKey = it.getString(ARG_KEY, presenter.argKey)
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_webview

    override val statusBarVisible: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appbarLayout.gone()

        webView.apply {
            settings.apply {
                setAppCacheEnabled(false)
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            webViewClient = customWebViewClient
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onDestroyView() {
        webView.webViewClient = null
        webView.stopLoading()
        super.onDestroyView()
    }

    override fun setRefreshing(refreshing: Boolean) {}

    override fun loadPage(url: String) {
        webView.loadUrl(url)
    }

    override fun showError() {
        AlertDialog.Builder(context!!)
            .setMessage("Не найден связанный аккаунт.\n\nЕсли у вас уже есть аккаунт на сайте AniLibria.tv, то привяжите этот аккаунт в личном кабинете.\n\nЕсли аккаунта нет, то зарегистрируйте его на сайте.")
            .setPositiveButton("Перейти") { _, _ ->
                Utils.externalLink("${apiConfig.siteUrl}/pages/cp.php")
            }
            .setNegativeButton("Отмена", null)
            .show()
            .setOnDismissListener {
                presenter.onUserUnderstandWhatToDo()
            }
    }

    private val customWebViewClient = object : WebViewClient() {

        var loadingFinished = true
        var redirect = false

        @Suppress("OverridingDeprecatedMember")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            Log.e("S_DEF_LOG", "OverrideUrlLoading: $url")
            if (!loadingFinished) {
                redirect = true
            }

            loadingFinished = false

            val result = presenter.onNewRedirectLink(url)
            if (result) {
                return true
            }
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loadingFinished = false
            progressBarWv.visible()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (!redirect) {
                loadingFinished = true
            }

            if (loadingFinished && !redirect) {
                progressBarWv.gone()
            } else {
                redirect = false
            }
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            progressBarWv.gone()
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
}