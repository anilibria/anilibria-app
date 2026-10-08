package ru.radiationx.anilibria.ui.presenter

import android.view.ViewGroup
import ru.radiationx.anilibria.ui.widget.FranchisePosterCardView
import androidx.leanback.widget.Presenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.LibriaCard
import ru.radiationx.shared_app.imageloader.showImageUrl

class LibriaCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = FranchisePosterCardView(parent.context)
        return LibriaCardViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        item ?: return
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
    private val containerView: FranchisePosterCardView,
) : Presenter.ViewHolder(containerView) {

    private val cardHeight by lazy {
        containerView.context.resources.getDimension(R.dimen.card_height).toInt()
    }
    private val cardReleaseWidth by lazy {
        containerView.context.resources.getDimension(R.dimen.card_release_width).toInt()
    }
    private val cardYoutubeWidth by lazy {
        containerView.context.resources.getDimension(R.dimen.card_youtube_width).toInt()
    }

    fun bind(item: LibriaCard) {
        containerView.franchiseOrdinal = item.franchiseOrdinal
        containerView.isCurrentRelease = item.isCurrentRelease
        containerView.contentDescription = listOfNotNull(
            item.title,
            item.franchiseOrdinal?.let { "№$it в порядке просмотра" },
            "Открыт сейчас".takeIf { item.isCurrentRelease },
        ).joinToString(". ")
        when (item.type) {
            is LibriaCard.Type.Release -> containerView.setMainImageDimensions(
                cardReleaseWidth,
                cardHeight
            )

            is LibriaCard.Type.Youtube -> containerView.setMainImageDimensions(
                cardYoutubeWidth,
                cardHeight
            )
        }
        containerView.mainImageView?.showImageUrl(item.image)
    }

    fun unbind() {
        containerView.franchiseOrdinal = null
        containerView.isCurrentRelease = false
        containerView.contentDescription = null
        containerView.mainImageView?.showImageUrl(null)
    }
}
