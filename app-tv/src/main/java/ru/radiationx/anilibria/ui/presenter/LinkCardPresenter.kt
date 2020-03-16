package ru.radiationx.anilibria.ui.presenter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import ru.radiationx.anilibria.LinkCard
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.anilibria.extension.getCompatColor
import ru.radiationx.anilibria.extension.getCompatDrawable

class LinkCardPresenter : Presenter() {
    companion object {

        private val cardratio = 370f / 260f
        private val cardratio_1 = 188f / 335f
        private val targetHeight = 370f

        private val CARD_WIDTH = (targetHeight / cardratio).toInt()
        private val CARD_HEIGHT = ((targetHeight / cardratio) * cardratio).toInt()
    }


    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.mainImage = cardView.getCompatDrawable(R.drawable.ic_link_card)?.mutate()?.apply {
            setTint(cardView.getCompatColor(R.color.dark_contrast_icon))
        }
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER)
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        item as LinkCard
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    }
}