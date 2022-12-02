package ru.radiationx.anilibria.ui.presenter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.LinkCard
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.extension.getCompatDrawable

class LinkCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardHeight = parent.context.resources.getDimension(R.dimen.card_height).toInt()
        val cardReleaseWidth =
            parent.context.resources.getDimension(R.dimen.card_release_width).toInt()

        val cardView = ImageCardView(parent.context)
        cardView.mainImage = cardView.getCompatDrawable(R.drawable.ic_link_card)?.mutate()?.apply {
            setTint(cardView.getCompatColor(R.color.dark_contrast_icon))
        }
        cardView.setMainImageDimensions(cardReleaseWidth, cardHeight)
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER)
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        item as LinkCard
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    }
}