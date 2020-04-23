package ru.radiationx.anilibria.ui.presenter

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import kotlinx.android.extensions.LayoutContainer
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.LibriaCard

class LibriaCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        return LibriaCardViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        item as LibriaCard
        viewHolder as LibriaCardViewHolder
        viewHolder.bind(item)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        viewHolder as LibriaCardViewHolder
        viewHolder.unbind()
    }
}

class LibriaCardViewHolder(
    override val containerView: ImageCardView
) : Presenter.ViewHolder(containerView), LayoutContainer {

    private val cardHeight by lazy { containerView.context.resources.getDimension(R.dimen.card_height).toInt() }
    private val cardReleaseWidth by lazy { containerView.context.resources.getDimension(R.dimen.card_release_width).toInt() }
    private val cardYoutubeWidth by lazy { containerView.context.resources.getDimension(R.dimen.card_youtube_width).toInt() }

    fun bind(item: LibriaCard) {
        if (containerView.mainImageView.tag != item.image) {
            when (item.type) {
                LibriaCard.Type.RELEASE -> containerView.setMainImageDimensions(cardReleaseWidth, cardHeight)
                LibriaCard.Type.YOUTUBE -> containerView.setMainImageDimensions(cardYoutubeWidth, cardHeight)
            }
            ImageLoader.getInstance().displayImage(item.image, containerView.mainImageView, object : SimpleImageLoadingListener() {
                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    super.onLoadingComplete(imageUri, view, loadedImage)
                    view?.tag = item.image
                }
            })
        }
    }

    fun unbind() {

    }
}