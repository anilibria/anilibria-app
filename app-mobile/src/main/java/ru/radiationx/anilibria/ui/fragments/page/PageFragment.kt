package ru.radiationx.anilibria.ui.fragments.page

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
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
import ru.radiationx.anilibria.presentation.page.PageViewModel
import ru.radiationx.anilibria.ui.common.Templates
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.data.analytics.features.PageAnalytics
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.toBase64
import ru.radiationx.shared.ktx.android.toException
import ru.radiationx.shared.ktx.android.visible
import ru.radiationx.shared_app.analytics.LifecycleTimeCounter
import ru.radiationx.shared_app.common.SystemUtils
import ru.radiationx.shared_app.di.DI
import ru.radiationx.shared_app.di.injectDependencies
import ru.radiationx.shared_app.di.viewModel
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageFragment : BaseFragment<FragmentWebviewBinding>(R.layout.fragment_webview),
    ExtendedWebView.JsLifeCycleListener {

    companion object {
        private const val ARG_PATH: String = "page_path"
        private const val ARG_TITLE: String = "page_title"
        private const val WEB_VIEW_SCROLL_Y = "wvsy"

        fun newInstance(pageTitle: String, title: String? = null) = PageFragment().putExtra {
            putString(ARG_PATH, pageTitle)
            putString(ARG_TITLE, title)
        }
    }

    private val useTimeCounter by lazy {
        LifecycleTimeCounter(pageAnalytics::useTime)
    }

    private val viewModel by viewModel<PageViewModel>()

    private var pageTitle: String? = null

    @Inject
    lateinit var appThemeController: AppThemeController

    @Inject
    lateinit var apiConfig: ApiConfig

    @Inject
    lateinit var systemUtils: SystemUtils

    @Inject
    lateinit var pageAnalytics: PageAnalytics

    private var webViewScrollPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.pagePath = it.getString(ARG_PATH, null)
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
            title = when (viewModel.pagePath) {
                PageApi.PAGE_PATH_TEAM -> "Команда проекта"
                PageApi.PAGE_PATH_DONATE -> "Поддержать"
                else -> pageTitle ?: "Статическая страница"
            }
            setNavigationOnClickListener { viewModel.onBackPressed() }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        binding.webView.setJsLifeCycleListener(this)

        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                systemUtils.externalLink(url.orEmpty())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                pageAnalytics.loaded()
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                pageAnalytics.error(error.toException())
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view?.url == request?.url?.toString()) {
                    pageAnalytics.error(errorResponse.toException(request))
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view?.url == request?.url?.toString()) {
                    pageAnalytics.error(error.toException(request))
                }
            }
        }

        savedInstanceState?.let {
            webViewScrollPos = it.getInt(WEB_VIEW_SCROLL_Y, 0)
        }

        val template = DI.get(Templates::class.java).staticPageTemplate
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
            binding.progressBarWv.visible(state.loading)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.state.mapNotNull { it.data }.distinctUntilChanged().onEach { data ->
            binding.webView.evalJs("ViewModel.setText('content','${data.content.toBase64()}');")
        }.launchIn(viewLifecycleOwner.lifecycleScope)
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
        binding.webView.let {
            outState.putInt(WEB_VIEW_SCROLL_Y, it.scrollY)
        }
    }

    override fun onDomContentComplete(actions: ArrayList<String>) {

    }

    override fun onPageComplete(actions: ArrayList<String>) {
        binding.webView.syncWithJs {
            binding.webView.scrollTo(0, webViewScrollPos)
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}