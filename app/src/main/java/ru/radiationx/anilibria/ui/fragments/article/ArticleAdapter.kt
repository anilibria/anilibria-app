package ru.radiationx.anilibria.ui.fragments.article

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import kotlinx.android.synthetic.main.fragment_article.*
import kotlinx.android.synthetic.main.item_article_content.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.BaseAdapter
import ru.radiationx.anilibria.ui.adapters.BaseViewHolder
import android.widget.LinearLayout
import ru.radiationx.anilibria.R.id.webView
import ru.radiationx.anilibria.App


/**
 * Created by mintrocket on 20.12.2017.
 */
class ArticleAdapter : BaseAdapter<String, BaseViewHolder<String>>() {
    private val ARTICLE_HEAD_LAYOUT = 1
    private val ARTICLE_CONTENT_LAYOUT = 2


    override fun onBindViewHolder(holder: BaseViewHolder<String>?, position: Int) {
        holder?.bind()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<String>? {
        when (viewType) {
            ARTICLE_HEAD_LAYOUT -> {
                return HeadHolder(inflateLayout(parent, R.layout.item_article_head))
            }
            ARTICLE_CONTENT_LAYOUT -> {
                return ContentHolder(inflateLayout(parent, R.layout.item_article_content))
            }
        }
        return null
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            ARTICLE_HEAD_LAYOUT
        } else ARTICLE_CONTENT_LAYOUT
    }

    override fun getItemCount(): Int {
        return 2
    }

    private class HeadHolder(itemView: View) : BaseViewHolder<String>(itemView) {

    }

    private class ContentHolder(itemView: View) : BaseViewHolder<String>(itemView) {

        init {
            initWebView(itemView.webView)
        }

        //@SuppressLint("JavascriptInterface")
        private fun initWebView(webView: WebView){
            webView.addJavascriptInterface(this@ContentHolder, "ALibApp")
            webView.settings.javaScriptEnabled = true

            val settings = webView.getSettings()
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL)
            settings.setBuiltInZoomControls(false)
            settings.setDefaultFontSize(16)
            settings.setTextZoom(100)
            settings.setJavaScriptEnabled(true)
            settings.setAllowFileAccess(true)
            settings.setAllowContentAccess(true)
            settings.setAllowFileAccessFromFileURLs(true)
            settings.setAllowUniversalAccessFromFileURLs(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)
            }


        }

        @JavascriptInterface
        fun resize(value: String) {
            Log.e("SUKA", "Webview resize " + value)
            val height = value.toFloat()
            Handler(Looper.getMainLooper()).post{
                itemView.webView.layoutParams.width = App.Companion.instance.resources.displayMetrics.widthPixels
                itemView.webView.layoutParams.height = (height * App.Companion.instance.resources.displayMetrics.density).toInt()
            }
        }

        override fun bind() {
            itemView.webView.loadUrl("file:///android_asset/test.html")
        }

    }
}
