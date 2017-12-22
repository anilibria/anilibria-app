package ru.radiationx.anilibria.ui.fragments.article

import android.graphics.*
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_article.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import kotlinx.android.synthetic.main.toolbar_content_article.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.ArticleFull
import ru.radiationx.anilibria.data.api.models.ArticleItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import java.util.ArrayList
import org.json.JSONException
import org.json.JSONObject
import ru.radiationx.anilibria.ui.widgets.ScrimHelper


/**
 * Created by radiationx on 20.12.17.
 */
class ArticleFragment : BaseFragment(), ArticleView, ExtendedWebView.JsLifeCycleListener {

    companion object {
        const val ARG_URL: String = "article_url"
        const val ARG_ITEM: String = "article_item"
    }

    override val layoutRes: Int = R.layout.fragment_article

    var currentColor: Int = Color.TRANSPARENT
    var currentTitle: String? = null

    @InjectPresenter
    lateinit var presenter: ArticlePresenter

    @ProvidePresenter
    fun provideArticlePresenter(): ArticlePresenter {
        return ArticlePresenter(App.injections.articlesRepository,
                (parentFragment as RouterProvider).router)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //presenter.loadArticle("novosti/17-12-2017-otchyet-komandy-po-relizam-za-nedelyu/")
        arguments?.let {
            presenter.url = it.getString(ARG_URL, "")
            (it.getSerializable(ARG_ITEM) as ArticleItem).let {
                presenter.setDataFromItem(it)
                presenter.url = it.url
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fitSystemWindow()
        val params = toolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
        toolbarLayout.layoutParams = params
        toolbarContent.visibility = View.VISIBLE
        View.inflate(toolbarContent.context, R.layout.toolbar_content_article, toolbarContent)
        //toolbarImage.setEnabledAspectRation(false)

        val scrimHelper = ScrimHelper(appbarLayout, toolbarLayout)
        scrimHelper.setScrimListener(object : ScrimHelper.ScrimListener {
            override fun onScrimChanged(scrim: Boolean) {
                if (scrim) {
                    toolbar.navigationIcon?.clearColorFilter()
                    toolbar.overflowIcon?.clearColorFilter()
                    toolbar.title = currentTitle
                    //toolbarTitleView.setVisibility(View.VISIBLE)
                } else {
                    toolbar.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                    toolbar.title = null
                    //toolbarTitleView.setVisibility(View.GONE)
                }
            }
        })

        toolbar.apply {
            //title = getString(R.string.fragment_title_release)
            setNavigationOnClickListener({
                presenter.onBackPressed()
            })
            setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
        }

        fixToolbarInsets(toolbar)
        setMarqueeTitle(toolbar)

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

    }

    override fun showArticle(article: ArticleFull) {
        currentTitle = article.title
        webView.evalJs("ViewModel.setText('content','${transformMessageSrc(article.content)}');")
    }

    fun transformMessageSrc(inSrc: String): String {
        var outSrc = inSrc
        outSrc = outSrc.replace("\n".toRegex(), "").replace("'".toRegex(), "&apos;")
        outSrc = JSONObject.quote(outSrc)
        outSrc = outSrc.substring(1, outSrc.length - 1)
        val jsonObject = JSONObject()
        try {
            jsonObject.put("src", outSrc)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return outSrc
    }

    override fun preShow(imageUrl: String, title: String, nick: String, comments: Int, views: Int) {
        currentTitle = title
        ImageLoader.getInstance().displayImage(imageUrl, toolbarImage, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap) {
                super.onLoadingComplete(imageUri, view, loadedImage)
                Single.defer {
                    Single.just(isDarkImage(loadedImage))
                }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ isDark ->
                            toolbar?.let {
                                Log.e("SUKA", "LOADED IMAGE TYPE " + isDark)
                                if (isDark) {
                                    currentColor = Color.WHITE
                                } else {
                                    currentColor = Color.BLACK
                                }
                                toolbar.navigationIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                                toolbar.overflowIcon?.setColorFilter(currentColor, PorterDuff.Mode.SRC_ATOP)
                            }
                        })

            }
        })



        webView.evalJs("ViewModel.setText('title','$title');")
        webView.evalJs("ViewModel.setText('nick','$nick');")
        webView.evalJs("ViewModel.setText('comments_count','$comments');")
        webView.evalJs("ViewModel.setText('views_count','$views');")
    }


}
