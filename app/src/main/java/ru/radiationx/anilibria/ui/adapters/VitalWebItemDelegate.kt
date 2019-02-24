package ru.radiationx.anilibria.ui.adapters

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_vital_web.view.*
import kotlinx.android.synthetic.main.item_vital_web_card.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.utils.Utils


/**
 * Created by radiationx on 13.01.18.
 */
class VitalWebItemDelegate(val inDetail: Boolean = false) : AdapterDelegate<MutableList<ListItem>>() {


    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is VitalWebListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as VitalWebListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_vital_web_card, parent, false)
    )

    private inner class ViewHolder(val holderView: View) : RecyclerView.ViewHolder(holderView) {

        lateinit var currentItem: VitalItem

        init {
            holderView.run {
                if (inDetail) {
                    holderView.item_card.cardElevation = 0f
                }
                vitalWebView.settings.apply {
                    layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                }
                vitalWebView.setOnTouchListener { _, event -> event.action == MotionEvent.ACTION_MOVE }
                vitalWebView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        webSwitcher.displayedChild = 0
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        webSwitcher.displayedChild = 1
                    }

                    @Suppress("OverridingDeprecatedMember")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        Utils.externalLink(url.toString())
                        return true
                    }
                }
            }
        }

        fun bind(item: VitalItem) {
            if (!::currentItem.isInitialized || currentItem != item) {
                currentItem = item
                holderView.run {
                    vitalWebView.easyLoadData(Api.WIDGETS_SITE_URL, item.contentText)
                }
            }
        }
    }
}