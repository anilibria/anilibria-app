package ru.radiationx.anilibria.ui.fragments.article

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.util.Base64
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_article.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.ArticleFull
import ru.radiationx.anilibria.data.api.models.ArticleItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.fragments.SharedReceiver
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import ru.radiationx.anilibria.ui.widgets.ScrimHelper
import ru.radiationx.anilibria.utils.ToolbarHelper
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * Created by radiationx on 20.12.17.
 */
class ArticleFragment : BaseFragment(), ArticleView, SharedReceiver, ExtendedWebView.JsLifeCycleListener {

    companion object {
        const val ARG_URL: String = "article_url"
        const val ARG_ITEM: String = "article_item"
    }

    private var currentColor: Int = Color.TRANSPARENT
    private var currentTitle: String? = null

    @InjectPresenter
    lateinit var presenter: ArticlePresenter

    @ProvidePresenter
    fun provideArticlePresenter(): ArticlePresenter {
        return ArticlePresenter(App.injections.articlesRepository,
                (parentFragment as RouterProvider).router)
    }

    override var transitionNameLocal = ""

    override fun setTransitionName(name: String) {
        transitionNameLocal = name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            presenter.url = it.getString(ARG_URL, "")
            (it.getSerializable(ARG_ITEM) as ArticleItem).let {
                presenter.setDataFromItem(it)
                presenter.url = it.url
            }
        }
    }

    override fun getLayoutResource(): Int = R.layout.fragment_article

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ToolbarHelper.setTransparent(toolbar, appbarLayout)
        ToolbarHelper.setScrollFlag(toolbarLayout, AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
        ToolbarHelper.fixInsets(toolbar)
        ToolbarHelper.marqueeTitle(toolbar)

        toolbar.apply {
            //title = getString(R.string.fragment_title_release)
            setNavigationOnClickListener({
                presenter.onBackPressed()
            })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        toolbarImage.visibility = View.VISIBLE
        toolbarImage.setAspectRatio(0.5f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarImage.transitionName = transitionNameLocal
        }

        val scrimHelper = ScrimHelper(appbarLayout, toolbarLayout)
        scrimHelper.setScrimListener(object : ScrimHelper.ScrimListener {
            override fun onScrimChanged(scrim: Boolean) {
                if (scrim) {
                    toolbar.navigationIcon?.clearColorFilter()
                    toolbar.overflowIcon?.clearColorFilter()
                    toolbar.title = currentTitle
                } else {
                    toolbar.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.title = null
                }
            }
        })

        val template = App.instance.articleTemplate
        webView.easyLoadData(Api.BASE_URL, template.generateOutput())
        template.reset()
    }

    override fun onDomContentComplete(actions: ArrayList<String>) {

    }

    override fun onPageComplete(actions: ArrayList<String>) {

    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return true
    }

    override fun setRefreshing(refreshing: Boolean) {
        progressSwitcher.displayedChild = if (refreshing) 1 else 0
    }

    override fun showArticle(article: ArticleFull) {
        currentTitle = article.title
        webView.evalJs("ViewModel.setText('content','${convert(article.content)}');")
    }

    private fun convert(string: String): String {
        return Base64.encodeToString(string.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
    }

    override fun preShow(imageUrl: String, title: String, nick: String, comments: Int, views: Int) {
        currentTitle = title
        ImageLoader.getInstance().displayImage(imageUrl, toolbarImage, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap) {
                super.onLoadingComplete(imageUri, view, loadedImage)
                ToolbarHelper.isDarkImage(loadedImage, Consumer<Boolean> {
                    if (it) {
                        currentColor = Color.WHITE
                    } else {
                        currentColor = Color.BLACK
                    }
                    toolbar.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                })
            }
        })

        webView.evalJs("ViewModel.setText('title','${convert(title)}');")
        webView.evalJs("ViewModel.setText('nick','${convert(nick)}');")
        webView.evalJs("ViewModel.setText('comments_count','${convert(comments.toString())}');")
        webView.evalJs("ViewModel.setText('views_count','${convert(views.toString())}');")
    }
}
