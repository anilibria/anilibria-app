package ru.radiationx.anilibria.ui.presenter

import android.view.View
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.extensions.LayoutContainer
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.data.entity.app.release.ReleaseItem

class ReleaseCardPresenter : Presenter() {


    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        return ReleaseViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        item as LibriaCard
        viewHolder as ReleaseViewHolder
        viewHolder.bind(item)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        viewHolder as ReleaseViewHolder
        viewHolder.unbind()
    }
}

class ReleaseViewHolder(
    override val containerView: ImageCardView
) : Presenter.ViewHolder(containerView), LayoutContainer {

    companion object {

        private val cardratio = 370f / 260f
        private val cardratio_1 = 188f / 335f
        private val targetHeight = 370f

        private val CARD_WIDTH = (targetHeight / cardratio).toInt()
        private val CARD_HEIGHT = ((targetHeight / cardratio) * cardratio).toInt()

        private val CARD_WIDTH_1 = (targetHeight / cardratio_1).toInt()
        private val CARD_HEIGHT_1 = ((targetHeight / cardratio_1) * cardratio_1).toInt()
    }

    fun bind(item: LibriaCard) {
        when (item.type) {
            LibriaCard.Type.RELEASE -> containerView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            LibriaCard.Type.YOUTUBE -> containerView.setMainImageDimensions(CARD_WIDTH_1, CARD_HEIGHT_1)
        }
        ImageLoader.getInstance().displayImage(item.image, containerView.mainImageView)
        containerView.cameraDistance = 1f
    }

    fun unbind() {

    }
}