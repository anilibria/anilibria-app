package ru.radiationx.anilibria.ui.fragments.page

import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.anilibria.databinding.FragmentWebviewBinding
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.extension.getWebStyleType
import ru.radiationx.anilibria.ui.common.Templates
import ru.radiationx.anilibria.ui.fragments.BaseToolbarFragment
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.data.analytics.features.PageAnalytics
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.data.entity.common.Url
import ru.radiationx.quill.get
import ru.radiationx.quill.inject
import ru.radiationx.quill.viewModel
import ru.radiationx.shared.ktx.android.WebResourceErrorCompat
import ru.radiationx.shared.ktx.android.WebResourceRequestCompat
import ru.radiationx.shared.ktx.android.WebViewClientCompat
import ru.radiationx.shared.ktx.android.getExtraNotNull
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.setWebViewClientCompat
import ru.radiationx.shared.ktx.android.toBase64
import ru.radiationx.shared.ktx.android.toException
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.common.SystemUtils
import timber.log.Timber

/**
 * Created by radiationx on 13.01.18.
 */
class PageFragment : BaseToolbarFragment<FragmentWebviewBinding>(R.layout.fragment_webview),
    ExtendedWebView.JsLifeCycleListener {

    companion object {
        private const val ARG_PATH: String = "page_path"
        private const val ARG_TITLE: String = "page_title"
        private const val WEB_VIEW_SCROLL_Y = "wvsy"

        fun newInstance(pagePath: Url.Relative, title: String? = null) = PageFragment().putExtra {
            putParcelable(ARG_PATH, pagePath)
            putString(ARG_TITLE, title)
        }
    }

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(pageAnalytics::useTime)
    }

    private val argPath by lazy { getExtraNotNull<Url.Relative>(ARG_PATH) }

    private val viewModel by viewModel<PageViewModel> {
        PageExtra(path = argPath)
    }

    private var pageTitle: String? = null

    private val appThemeController by inject<AppThemeController>()

    private val apiConfig by inject<ApiConfig>()

    private val systemUtils by inject<SystemUtils>()

    private val pageAnalytics by inject<PageAnalytics>()

    private var webViewScrollPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageTitle = it.getString(ARG_TITLE, null)
        }
    }

    override fun onCreateBinding(view: View): FragmentWebviewBinding {
        return FragmentWebviewBinding.bind(view)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(useTimeCounter)
        //ToolbarHelper.setTransparent(toolbar, appbarLayout)
        //ToolbarHelper.setScrollFlag(toolbarLayout, AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
        ToolbarHelper.fixInsets(baseBinding.toolbar)
        ToolbarHelper.marqueeTitle(baseBinding.toolbar)

        baseBinding.toolbar.apply {
            val rawPath = argPath.raw
            title = when {
                pageTitle != null -> pageTitle
                rawPath.contains(PageApi.PAGE_PATH_TEAM) -> "Команда проекта"
                rawPath.contains(PageApi.PAGE_PATH_DONATE) -> "Поддержать"
                else -> "Статическая страница"
            }
            setNavigationOnClickListener { viewModel.onBackPressed() }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        binding.webView.setJsLifeCycleListener(this)

        val webViewClient = object : WebViewClientCompat() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequestCompat,
            ): Boolean {
                systemUtils.open(request.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                pageAnalytics.loaded()
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError,
            ) {
                super.onReceivedSslError(view, handler, error)
                onPageError(error.toException())
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequestCompat,
                errorResponse: WebResourceResponse,
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (view.url == request.url.toString()) {
                    onPageError(errorResponse.toException(request))
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequestCompat,
                error: WebResourceErrorCompat,
            ) {
                super.onReceivedError(view, request, error)
                if (view.url == request.url.toString()) {
                    onPageError(error.toException(request))
                }
            }
        }
        binding.webView.setWebViewClientCompat(webViewClient)

        savedInstanceState?.let {
            webViewScrollPos = it.getInt(WEB_VIEW_SCROLL_Y, 0)
        }

        val template = get<Templates>().staticPageTemplate
        binding.webView.easyLoadData(
            apiConfig.siteUrl,
            template.generateWithTheme(appThemeController.getTheme())
        )

        appThemeController
            .observeTheme()
            .onEach {
                binding.webView.evalJs("changeStyleType(\"${it.getWebStyleType()}\")")
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state.onEach { state ->
            binding.progressBarWv.isVisible = state.loading
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state.mapNotNull { it.data }.distinctUntilChanged().onEach { data ->
            binding.webView.evalJs("ViewModel.setText('content','${data.content.toBase64()}');")
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onPageError(error: Exception) {
        Timber.e(error, "onPageError")
        pageAnalytics.error()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (view != null) {
            outState.putInt(WEB_VIEW_SCROLL_Y, binding.webView.scrollY)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.endWork()
        binding.webView.setJsLifeCycleListener(null)
        binding.webView.setWebViewClientCompat(null)
    }

    override fun onDomContentComplete(actions: ArrayList<String>) {

    }

    override fun onPageComplete(actions: ArrayList<String>) {
        binding.webView.syncWithJs {
            binding.webView.scrollTo(0, webViewScrollPos)
        }
    }
}