package ru.radiationx.anilibria.ui.fragments.comments

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.anilibria.databinding.FragmentVkCommentsBinding
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.extension.getWebStyleType
import ru.radiationx.anilibria.ui.common.Templates
import ru.radiationx.anilibria.ui.common.webpage.WebPageStateWebViewClient
import ru.radiationx.anilibria.ui.common.webpage.WebPageViewState
import ru.radiationx.anilibria.ui.common.webpage.compositeWebViewClientOf
import ru.radiationx.anilibria.ui.fragments.BaseDimensionsFragment
import ru.radiationx.anilibria.ui.fragments.TopScroller
import ru.radiationx.anilibria.ui.fragments.comments.webview.VkWebChromeClient
import ru.radiationx.anilibria.ui.fragments.comments.webview.VkWebViewClient
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.utils.dimensions.Dimensions
import ru.radiationx.data.MainClient
import ru.radiationx.data.datasource.remote.IClient
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.launchInResumed
import ru.radiationx.shared.ktx.android.setWebViewClientCompat
import ru.radiationx.shared.ktx.android.toBase64
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber


class VkCommentsFragment : BaseDimensionsFragment(R.layout.fragment_vk_comments), TopScroller {

    companion object {
        const val WEB_VIEW_SCROLL_Y = "wvsy"
    }

    private var webViewScrollPos = 0

    private val binding by viewBinding<FragmentVkCommentsBinding>()

    private val viewModel by viewModel<VkCommentsViewModel>()

    private val appThemeController by inject<AppThemeController>()

    private val systemUtils by inject<SystemUtils>()

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

        binding.webView.setJsLifeCycleListener(jsLifeCycleListener(binding.webView))

        binding.webView.settings.apply {
            this.databaseEnabled = true
        }


        val webViewClient = composite(
            viewModel = viewModel,
            systemUtils = systemUtils,
            networkClient = get(MainClient::class),
            commentsCss = get(),
            appThemeController = appThemeController
        )
        binding.webView.setWebViewClientCompat(webViewClient)
        binding.webView.webChromeClient = VkWebChromeClient(viewModel)

        val cookieManager = CookieManager.getInstance()

        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)

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
        }.launchInResumed(viewLifecycleOwner)
    }

    override fun updateDimens(dimensions: Dimensions) {
        super.updateDimens(dimensions)
        binding.root.updatePadding(
            left = dimensions.left,
            right = dimensions.right
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (view != null) {
            outState.putInt(WEB_VIEW_SCROLL_Y, binding.webView.scrollY)
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
        super.onDestroyView()
        binding.webView.setJsLifeCycleListener(null)
        binding.webView.setWebViewClientCompat(null)
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.endWork()
    }

    override fun scrollToTop() {
        binding.webView.scrollY = 0
    }

    private fun showState(state: VkCommentsScreenState) {
        val anyLoading = state.data.loading || state.pageState == WebPageViewState.Loading
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
        val template = get<Templates>().vkCommentsTemplate
        binding.webView.easyLoadData(
            comments.url,
            template.generateWithTheme(appThemeController.getTheme())
        )
        binding.webView.evalJs("ViewModel.setText('content','${comments.script.toBase64()}');")
    }

    private fun jsLifeCycleListener(
        webView: ExtendedWebView,
    ) = object : ExtendedWebView.JsLifeCycleListener {
        override fun onDomContentComplete(actions: ArrayList<String>?) {
        }

        override fun onPageComplete(actions: ArrayList<String>?) {
            webView.syncWithJs {
                webView.scrollTo(0, webViewScrollPos)
            }
        }
    }

    @JavascriptInterface
    fun log(string: String) {
        Timber.tag("VkCommentsFragment.log").d(string)
    }


    private fun composite(
        viewModel: VkCommentsViewModel,
        systemUtils: SystemUtils,
        networkClient: IClient,
        commentsCss: VkCommentsCss,
        appThemeController: AppThemeController,
    ) = compositeWebViewClientOf(
        WebPageStateWebViewClient {
            viewModel.onNewPageState(it)
        },
        VkWebViewClient(viewModel, systemUtils, networkClient, commentsCss, appThemeController)
    )
}