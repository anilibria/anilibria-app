package ru.radiationx.anilibria.ui.fragments.page

import android.os.Bundle
import android.view.View
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.fragment_webview.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.shared_app.getDependency
import ru.radiationx.shared_app.injectDependencies
import ru.radiationx.anilibria.extension.generateWithTheme
import ru.radiationx.anilibria.extension.getWebStyleType
import ru.radiationx.anilibria.presentation.page.PagePresenter
import ru.radiationx.anilibria.presentation.page.PageView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.utils.ToolbarHelper
import ru.radiationx.data.datasource.holders.AppThemeHolder
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.datasource.remote.api.PageApi
import ru.radiationx.data.entity.app.page.PageLibria
import ru.radiationx.shared.ktx.android.putExtra
import ru.radiationx.shared.ktx.android.toBase64
import ru.radiationx.shared.ktx.android.visible
import java.util.*
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageFragment : BaseFragment(), PageView, ExtendedWebView.JsLifeCycleListener {

    companion object {
        private const val ARG_PATH: String = "page_path"
        private const val ARG_TITLE: String = "page_title"
        private const val WEB_VIEW_SCROLL_Y = "wvsy"

        fun newInstance(pageTitle: String, title: String? = null) = PageFragment().putExtra {
            putString(ARG_PATH, pageTitle)
            putString(ARG_TITLE, title)
        }
    }

    private var pageTitle: String? = null

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

    @Inject
    lateinit var apiConfig: ApiConfig

    private val disposables = CompositeDisposable()

    private var webViewScrollPos = 0

    @InjectPresenter
    lateinit var presenter: PagePresenter

    @ProvidePresenter
    fun providePagePresenter(): PagePresenter = getDependency(screenScope, PagePresenter::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies(screenScope)
        super.onCreate(savedInstanceState)
        arguments?.let {
            presenter.pagePath = it.getString(ARG_PATH, null)
            pageTitle = it.getString(ARG_TITLE, null)
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_webview

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ToolbarHelper.setTransparent(toolbar, appbarLayout)
        //ToolbarHelper.setScrollFlag(toolbarLayout, AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
        ToolbarHelper.fixInsets(toolbar)
        ToolbarHelper.marqueeTitle(toolbar)

        toolbar.apply {
            title = when (presenter.pagePath) {
                PageApi.PAGE_PATH_TEAM -> "Список команды"
                PageApi.PAGE_PATH_DONATE -> "Поддержать"
                else -> pageTitle ?: "Статическая страница"
            }
            setNavigationOnClickListener { presenter.onBackPressed() }
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        webView.setJsLifeCycleListener(this)

        savedInstanceState?.let {
            webViewScrollPos = it.getInt(WEB_VIEW_SCROLL_Y, 0)
        }

        val template = App.instance.staticPageTemplate
        webView.easyLoadData(apiConfig.siteUrl, template.generateWithTheme(appThemeHolder.getTheme()))

        disposables.add(
                appThemeHolder
                        .observeTheme()
                        .subscribe {
                            webView?.evalJs("changeStyleType(\"${it.getWebStyleType()}\")")
                        }
        )
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView?.let {
            outState.putInt(WEB_VIEW_SCROLL_Y, it.scrollY)
        }
    }

    override fun onDomContentComplete(actions: ArrayList<String>) {

    }

    override fun onPageComplete(actions: ArrayList<String>) {
        webView?.syncWithJs {
            webView?.scrollTo(0, webViewScrollPos)
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setRefreshing(refreshing: Boolean) {
        progressBarWv.visible(refreshing)
    }

    override fun showPage(page: PageLibria) {
        //toolbar.title = page.title
        webView?.evalJs("ViewModel.setText('content','${page.content.toBase64()}');")
    }

}