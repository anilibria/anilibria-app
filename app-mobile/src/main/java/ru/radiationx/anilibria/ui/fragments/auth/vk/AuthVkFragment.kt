package ru.radiationx.anilibria.ui.fragments.auth.vk

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebViewClient
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
import ru.radiationx.anilibria.ui.fragments.auth.AuthPatternWebViewClient
import ru.radiationx.quill.quillViewModel
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.gone
import ru.radiationx.shared.ktx.android.putExtra

class AuthVkFragment : BaseToolbarFragment<FragmentAuthSocialBinding>(R.layout.fragment_auth_social) {
    companion object {
        private const val ARG_URL = "ARG_SOCIAL_URL"

        fun newInstance(url: String) = AuthVkFragment().putExtra {
            putString(ARG_URL, url)
        }
    }

    private val authPatternWebViewClient by lazy { AuthPatternWebViewClient(viewModel::onSuccessAuthResult) }

    private val webPageWebViewClient by lazy { WebPageStateWebViewClient(viewModel::onPageStateChanged) }

    private val compositeWebViewClient by lazy {
        compositeWebViewClientOf(
            authPatternWebViewClient,
            webPageWebViewClient
        )
    }

    private val viewModel by quillViewModel<AuthVkViewModel>{
        AuthVkExtra(url = getExtraNotNull(ARG_URL))
    }

    override val statusBarVisible: Boolean = true

    override fun onCreateBinding(view: View): FragmentAuthSocialBinding {
        return FragmentAuthSocialBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseBinding.appbarLayout.gone()

        binding.errorView.setPrimaryButtonClickListener {
            binding.webView.reload()
        }

        binding.cookieView.setPrimaryButtonClickListener {
            viewModel.onContinueClick()
        }
        binding.cookieView.setSecondaryClickListener {
            viewModel.onClearDataClick()
        }

        binding.webView.settings.apply {
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        binding.webView.webViewClient = compositeWebViewClient

        viewModel.state.mapNotNull { it.data }.distinctUntilChanged().onEach { data ->
            authPatternWebViewClient.resultPattern = data.pattern
            binding.webView.loadUrl(data.url)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state.onEach { state ->
            binding.progressBarWv.isVisible = state.pageState == WebPageViewState.Loading
            binding.webView.isVisible =
                state.pageState == WebPageViewState.Success && !state.showClearCookies
            binding.errorView.isVisible = state.pageState is WebPageViewState.Error
            binding.cookieView.isVisible = state.showClearCookies
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        binding.webView.webViewClient = WebViewClient()
        binding.webView.stopLoading()
        super.onDestroyView()
    }
}