package ru.radiationx.anilibria.ui.fragments.auth.social

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.FragmentAuthSocialBinding
import ru.radiationx.anilibria.ui.common.webpage.WebPageStateWebViewClient
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.common.webpage.compositeWebViewClientOf
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.fragments.auth.AnalyticsWebViewClient
import ru.radiationx.anilibria.ui.fragments.auth.AuthPatternWebViewClient
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.common.SystemUtils


/**
 * Created by radiationx on 31.12.17.
 */
class AuthSocialFragment : BaseToolbarFragment<FragmentAuthSocialBinding>(R.layout.fragment_auth_social) {

    companion object {
        private const val ARG_KEY = "key"

        fun newInstance(key: String) = AuthSocialFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_KEY, key)
            }
        }
    }

    private val authPatternWebViewClient by lazy {
        AuthPatternWebViewClient(viewModel::onSuccessAuthResult)
    }

    private val analyticsWebViewClient by lazy {
        AnalyticsWebViewClient(viewModel::sendAnalyticsPageError)
    }

    private val webPageWebViewClient by lazy {
        WebPageStateWebViewClient(viewModel::onPageStateChanged)
    }

    private val compositeWebViewClient by lazy {
        compositeWebViewClientOf(
            authPatternWebViewClient,
            analyticsWebViewClient,
            webPageWebViewClient
        )
    }

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(viewModel::submitUseTime)
    }

    private val viewModel by viewModel<AuthSocialViewModel> {
        AuthSocialExtra(key = getExtraNotNull(ARG_KEY))
    }

    private val apiConfig by inject<ApiConfig>()

    private val systemUtils by inject<SystemUtils>()

    override val statusBarVisible: Boolean = true

    override fun onCreateBinding(view: View): FragmentAuthSocialBinding {
        return FragmentAuthSocialBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(useTimeCounter)
        baseBinding.appbarLayout.isGone = true

        binding.webView.apply {
            settings.apply {
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            webViewClient = compositeWebViewClient
        }

        binding.errorView.setPrimaryButtonClickListener {
            binding.webView.reload()
        }

        binding.cookieView.setPrimaryButtonClickListener {
            viewModel.onContinueClick()
        }
        binding.cookieView.setSecondaryClickListener {
            viewModel.onClearDataClick()
        }

        viewModel.state.mapNotNull { it.data }.distinctUntilChanged().onEach { data ->
            authPatternWebViewClient.resultPattern = data.resultPattern
            binding.webView.loadUrl(data.socialUrl)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state.onEach { state ->
            val anyLoading = state.isAuthProgress || state.pageState == WebPageViewState.Loading
            binding.progressBarWv.isVisible = anyLoading
            binding.webView.isVisible =
                state.pageState == WebPageViewState.Success && !anyLoading && !state.showClearCookies
            binding.errorView.isVisible = state.pageState is WebPageViewState.Error
            binding.cookieView.isVisible = state.showClearCookies
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.errorEvent.onEach {
            showError()
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        binding.webView.webViewClient = WebViewClient()
        binding.webView.stopLoading()
        super.onDestroyView()
    }

    private fun showError() {
        AlertDialog.Builder(requireContext())
            .setMessage("Не найден связанный аккаунт.\n\nЕсли у вас уже есть аккаунт на сайте AniLibria.tv, то привяжите этот аккаунт в личном кабинете.\n\nЕсли аккаунта нет, то зарегистрируйте его на сайте.")
            .setPositiveButton("Перейти") { _, _ ->
                systemUtils.externalLink("${apiConfig.siteUrl}/pages/cp.php")
            }
            .setNegativeButton("Отмена", null)
            .show()
            .setOnDismissListener {
                viewModel.onUserUnderstandWhatToDo()
            }
    }
}