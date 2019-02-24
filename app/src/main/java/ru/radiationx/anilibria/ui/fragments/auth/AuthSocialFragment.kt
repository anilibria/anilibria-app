package ru.radiationx.anilibria.ui.fragments.auth

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.fragment_auth_social.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.presentation.auth.social.AuthSocialPresenter
import ru.radiationx.anilibria.presentation.auth.social.AuthSocialView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.utils.Utils


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

    @InjectPresenter
    lateinit var presenter: AuthSocialPresenter

    @ProvidePresenter
    fun providePresenter(): AuthSocialPresenter = getDependency(screenScope, AuthSocialPresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            presenter.argKey = it.getString(ARG_KEY, presenter.argKey)
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_auth_social

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appbarLayout.visibility = View.GONE

        setStatusBarVisibility(true)
        setStatusBarColor(view.context.getColorFromAttr(R.attr.cardBackground))

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
                .setPositiveButton("Перейти") { dialog, which ->
                    Utils.externalLink("${Api.SITE_URL}/pages/cp.php")
                    dialog.dismiss()
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
            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (!redirect) {
                loadingFinished = true
            }

            if (loadingFinished && !redirect) {
                progressBar.visibility = View.GONE
            } else {
                redirect = false
            }
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            progressBar.visibility = View.GONE
        }
    }
}