package ru.radiationx.anilibria.ui.fragments.auth.vk

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_auth_social.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.auth.vk.AuthVkPresenter
import ru.radiationx.anilibria.presentation.auth.vk.AuthVkView
import ru.radiationx.anilibria.ui.common.webpage.WebPageStateWebViewClient
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.common.webpage.compositeWebViewClientOf
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.auth.AuthPatternWebViewClient
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared_app.di.injectDependencies

class AuthVkFragment : BaseFragment(), AuthVkView {
    companion object {
        private const val ARG_URL = "ARG_SOCIAL_URL"

        fun newInstance(url: String) = AuthVkFragment().putExtra {
            putString(ARG_URL, url)
        }
    }

    private val authPatternWebViewClient by lazy { AuthPatternWebViewClient(presenter::onSuccessAuthResult) }

    private val webPageWebViewClient by lazy { WebPageStateWebViewClient(presenter::onPageStateChanged) }

    private val compositeWebViewClient by lazy {
        compositeWebViewClientOf(
            authPatternWebViewClient,
            webPageWebViewClient
        )
    }

    @InjectPresenter
    lateinit var presenter: AuthVkPresenter

    @ProvidePresenter
    fun providePresenter(): AuthVkPresenter =
        getDependency(AuthVkPresenter::class.java, screenScope)

    override fun getLayoutResource(): Int = R.layout.fragment_auth_social

    override val statusBarVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            presenter.argUrl = it.getString(ARG_URL, presenter.argUrl)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appbarLayout.gone()

        errorView.setPrimaryButtonClickListener {
            webView.reload()
        }

        cookieView.setPrimaryButtonClickListener {
            presenter.onContinueClick()
        }
        cookieView.setSecondaryClickListener {
            presenter.onClearDataClick()
        }

        webView.settings.apply {
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webView.webViewClient = compositeWebViewClient
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onDestroyView() {
        webView.webViewClient = WebViewClient()
        webView.stopLoading()
        super.onDestroyView()
    }

    override fun loadPage(url: String, resultPattern: String) {
        authPatternWebViewClient.resultPattern = resultPattern
        webView.loadUrl(url)
    }

    override fun showState(state: AuthVkScreenState) {
        progressBarWv.isVisible = state.pageState == WebPageViewState.Loading
        webView.isVisible = state.pageState == WebPageViewState.Success && !state.showClearCookies
        errorView.isVisible = state.pageState is WebPageViewState.Error
        cookieView.isVisible = state.showClearCookies
    }
}