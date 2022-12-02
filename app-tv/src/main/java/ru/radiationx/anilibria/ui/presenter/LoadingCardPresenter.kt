package ru.radiationx.anilibria.ui.presenter

import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.ui.widget.CardLoadingView

class LoadingCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardHeight = parent.context.resources.getDimension(R.dimen.card_height).toInt()
        val cardReleaseWidth =
            parent.context.resources.getDimension(R.dimen.card_release_width).toInt()

        val loadingView = CardLoadingView(parent.context)
        loadingView.layoutParams = ViewGroup.LayoutParams(cardReleaseWidth, cardHeight)
        return ViewHolder(loadingView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        item as LoadingCard
        val loadingView = (viewHolder.view as CardLoadingView)
        loadingView.setState(
            if (item.isError) {
                CardLoadingView.State.ERROR
            } else {
                CardLoadingView.State.LOADING
            }
        )
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    }
}