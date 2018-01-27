package ru.radiationx.anilibria.ui.adapters

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import android.webkit.WebViewClient
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.android.synthetic.main.item_vital_native.view.*
import kotlinx.android.synthetic.main.item_vital_native_card.view.*
import ru.radiationx.anilibria.utils.Utils


/**
 * Created by radiationx on 13.01.18.
 */
class VitalNativeItemDelegate(val inDetail: Boolean = false) : AdapterDelegate<MutableList<ListItem>>() {


    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is VitalNativeListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as VitalNativeListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_vital_native_card, parent, false)
    )

    private inner class ViewHolder(val holderView: View) : RecyclerView.ViewHolder(holderView) {

        lateinit var currentItem: VitalItem

        init {
            if (inDetail) {
                holderView.item_card.cardElevation = 0f
            }
            holderView.setOnClickListener {
                currentItem.contentLink?.let { it1 -> Utils.externalLink(it1) }
            }
        }

        fun bind(item: VitalItem) {
            currentItem = item
            holderView.run {
                ImageLoader.getInstance().displayImage(item.contentImage, vitalImageView, object : SimpleImageLoadingListener() {
                    override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                        imageSwitcher.displayedChild = 0
                    }

                    override fun onLoadingStarted(imageUri: String?, view: View?) {
                        imageSwitcher.displayedChild = 1
                    }

                    override fun onLoadingCancelled(imageUri: String?, view: View?) {
                        imageSwitcher.displayedChild = 1
                    }

                    override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                        imageSwitcher.displayedChild = 1
                    }
                }
                )
            }
        }
    }
}