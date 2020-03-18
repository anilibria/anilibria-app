package ru.radiationx.anilibria.ui.presenter

import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import ru.radiationx.anilibria.common.LoadingCard
import ru.radiationx.anilibria.ui.widget.CardLoadingView

class LoadingCardPresenter : Presenter() {
    companion object {

        private val cardratio = 370f / 260f
        private val cardratio_1 = 188f / 335f
        private val targetHeight = 370f

        private val CARD_WIDTH = (targetHeight / cardratio).toInt()
        private val CARD_HEIGHT = ((targetHeight / cardratio) * cardratio).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val loadingView = CardLoadingView(parent.context)
        loadingView.layoutParams = ViewGroup.LayoutParams(CARD_WIDTH, CARD_HEIGHT)
        return ViewHolder(loadingView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        item as LoadingCard
        val loadingView = (viewHolder.view as CardLoadingView)
        loadingView.setState(
            if (item.errorTitle.isEmpty()) {
                CardLoadingView.State.LOADING
            } else {
                CardLoadingView.State.REFRESH
            }
        )
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
    }
}