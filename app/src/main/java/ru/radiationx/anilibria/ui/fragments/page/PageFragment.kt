package ru.radiationx.anilibria.ui.fragments.page

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.util.Base64
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.android.synthetic.main.fragment_article.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.article.ArticleFull
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.page.PageLibria
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.api.PageApi
import ru.radiationx.anilibria.presentation.article.details.ArticlePresenter
import ru.radiationx.anilibria.presentation.article.details.ArticleView
import ru.radiationx.anilibria.presentation.page.PagePresenter
import ru.radiationx.anilibria.presentation.page.PageView
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedReceiver
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import java.nio.charset.StandardCharsets
import java.util.ArrayList

/**
 * Created by radiationx on 13.01.18.
 */
class PageFragment : BaseFragment(), PageView, ExtendedWebView.JsLifeCycleListener {

    companion object {
        const val ARG_ID: String = "page_id"
        private const val WEB_VIEW_SCROLL_Y = "wvsy"
    }

    private var webViewScrollPos = 0

    @InjectPresenter
    lateinit var presenter: PagePresenter

    @ProvidePresenter
    fun providePagePresenter(): PagePresenter {
        return PagePresenter(App.injections.pageRepository,
                (parentFragment as RouterProvider).router)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            presenter.pageId = it.getString(ARG_ID, null)
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_article

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
        webView.easyLoadData(Api.BASE_URL, template.generateOutput())
        template.reset()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(WEB_VIEW_SCROLL_Y, webView.scrollY)
    }

    override fun onDomContentComplete(actions: ArrayList<String>) {

    }

    override fun onPageComplete(actions: ArrayList<String>) {
        webView.syncWithJs {
            webView.scrollTo(0, webViewScrollPos)
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setRefreshing(refreshing: Boolean) {
        progressSwitcher.displayedChild = if (refreshing) 1 else 0
    }

    override fun showPage(page: PageLibria) {
        //toolbar.title = page.title
        webView.evalJs("ViewModel.setText('content','${convert(page.content)}');")
    }

    private fun convert(string: String): String {
        return Base64.encodeToString(string.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
    }
}