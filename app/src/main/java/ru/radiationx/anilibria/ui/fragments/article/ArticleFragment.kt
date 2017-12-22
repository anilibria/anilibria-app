package ru.radiationx.anilibria.ui.fragments.article

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
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
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.ArticleFull
import ru.radiationx.anilibria.data.api.models.ArticleItem
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment
import ru.radiationx.anilibria.ui.widgets.ExtendedWebView
import java.util.ArrayList
import org.json.JSONException
import org.json.JSONObject


/**
 * Created by radiationx on 20.12.17.
 */
class ArticleFragment : BaseFragment(), ArticleView, ExtendedWebView.JsLifeCycleListener {

    companion object {
        const val ARG_URL: String = "article_url"
        const val ARG_ITEM: String = "article_item"
    }

    override val layoutRes: Int = R.layout.fragment_article

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
        //toolbar.title = article.title
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
        //toolbar.title = title
        ImageLoader.getInstance().displayImage(imageUrl, toolbarImage, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap) {
                super.onLoadingComplete(imageUri, view, loadedImage)
                val targetImage = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.width/2, loadedImage.height)
                val isDark =  isDarkImage(targetImage)
                Log.e("SUKA", "LOADED IMAGE TYPE " + isDark)
                if(isDark){
                    toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                }else{
                    toolbar.navigationIcon?.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
                }
            }
        })
        toolbarImage.visibility = View.VISIBLE
        webView.evalJs("ViewModel.setText('title','$title');")
        webView.evalJs("ViewModel.setText('nick','$nick');")
        webView.evalJs("ViewModel.setText('comments_count','$comments');")
        webView.evalJs("ViewModel.setText('views_count','$views');")
    }

    fun isDarkImage(bitmap: Bitmap): Boolean {
        val histogram = IntArray(256, { i: Int -> 0 })

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                //val color = bitmap.getRGB(x, y)

                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val brightness = (0.2126 * r + 0.7152 * g + 0.0722 * b).toInt()
                histogram[brightness]++;
            }
        }

        val allPixelsCount = bitmap.width * bitmap.height;
        val darkPixelCount = (0 until 64).sumBy { histogram[it] }
        return darkPixelCount > allPixelsCount * 0.25
    }
}
