package ru.radiationx.anilibria.ui.adapters

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.View
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_vital_native.*
import kotlinx.android.synthetic.main.item_vital_native_card.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.Utils


/**
 * Created by radiationx on 13.01.18.
 */
class VitalNativeItemDelegate(val inDetail: Boolean = false) : AppAdapterDelegate<VitalNativeListItem, ListItem, VitalNativeItemDelegate.ViewHolder>(
        R.layout.item_vital_native_card,
        { it is VitalNativeListItem },
        { ViewHolder(it, inDetail) }
) {

    override fun bindData(item: VitalNativeListItem, holder: ViewHolder) = holder.bind(item.item)

    class ViewHolder(
            override val containerView: View,
            inDetail: Boolean
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        lateinit var currentItem: VitalItem

        init {
            if (inDetail) {
                item_card.cardElevation = 0f
            }
            containerView.setOnClickListener {
                currentItem.contentLink?.let { it1 -> Utils.externalLink(it1) }
            }
        }

        fun bind(item: VitalItem) {
            currentItem = item
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
            })
        }
    }
}