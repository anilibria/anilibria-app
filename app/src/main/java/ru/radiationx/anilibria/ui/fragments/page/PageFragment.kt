package ru.radiationx.anilibria.ui.fragments.page

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.di.extensions.getDependency
import ru.radiationx.anilibria.di.extensions.injectDependencies
import ru.radiationx.anilibria.entity.app.page.PageLibria
import ru.radiationx.anilibria.extension.*
import ru.radiationx.anilibria.model.data.holders.AppThemeHolder
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.presentation.page.PagePresenter
import ru.radiationx.anilibria.presentation.page.PageView
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.utils.ToolbarHelper
import java.util.*
import javax.inject.Inject

/**
 * Created by radiationx on 13.01.18.
 */
class PageFragment : BaseFragment(), PageView, ExtendedWebView.JsLifeCycleListener {

    companion object {
        private const val ARG_ID: String = "page_id"
        private const val WEB_VIEW_SCROLL_Y = "wvsy"

        fun newInstance(pageId: String) = PageFragment().putExtra {
            putString(ARG_ID, pageId)
        }
    }

    @Inject
    lateinit var appThemeHolder: AppThemeHolder

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
            presenter.pageId = it.getString(ARG_ID, null)
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
            title = when (presenter.pageId) {
                PageApi.PAGE_ID_TEAM -> "Список команды"
                PageApi.PAGE_ID_BID -> "Подать заявку"
                PageApi.PAGE_ID_DONATE -> "Поддержать"
                PageApi.PAGE_ID_ABOUT_ANILIB -> "Об AniLibria"
                PageApi.PAGE_ID_RULES -> "Правила"
                else -> "Статическая страница"
            }
            setNavigationOnClickListener({
                presenter.onBackPressed()
            })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        webView.setJsLifeCycleListener(this)

        savedInstanceState?.let {
            webViewScrollPos = it.getInt(WEB_VIEW_SCROLL_Y, 0)
        }

        val template = App.instance.staticPageTemplate
        webView.easyLoadData(Api.SITE_URL, template.generateWithTheme(appThemeHolder.getTheme()))

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
        progressBar.visible(refreshing)
    }

    override fun showPage(page: PageLibria) {
        //toolbar.title = page.title
        webView?.evalJs("ViewModel.setText('content','${page.content.toBase64()}');")
    }

}