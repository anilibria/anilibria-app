package ru.radiationx.anilibria.ui.adapters.release.detail

import android.text.Html
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_head_new.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.release.details.ReleaseDetailModifiersState
import ru.radiationx.anilibria.presentation.release.details.ReleaseFavoriteState
import ru.radiationx.anilibria.presentation.release.details.ReleaseInfoState
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseHeadListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.LinkMovementMethod
import ru.radiationx.shared.ktx.android.setCompatDrawable

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseHeadDelegate(
    private val itemListener: Listener
) : AppAdapterDelegate<ReleaseHeadListItem, ListItem, ReleaseHeadDelegate.ViewHolder>(
    R.layout.item_release_head_new,
    { it is ReleaseHeadListItem },
    { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: ReleaseHeadListItem, holder: ViewHolder) =
        holder.bind(item.item, item.modifiers)

    class ViewHolder(
        override val containerView: View,
        private val itemListener: Listener
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            full_fav_btn.setOnClickListener {
                itemListener.onClickFav()
            }
            full_days_bar.clickListener = {
                itemListener.onScheduleClick(it)
            }
            full_info.movementMethod = LinkMovementMethod {
                itemListener.onClickTag(it)
                true
            }
            full_announce.movementMethod = LinkMovementMethod {
                itemListener.onClickSomeLink(it)
                true
            }
            full_description.movementMethod = LinkMovementMethod {
                itemListener.onClickSomeLink(it)
                true
            }
            full_description_expander.setOnClickListener {
                itemListener.onExpandClick()
            }
        }

        fun bind(state: ReleaseInfoState, modifiers: ReleaseDetailModifiersState) {
            full_title.text = state.titleRus
            full_title_en.text = state.titleEng
            full_description.text = Html.fromHtml(state.description)
            full_description.doOnLayout {
                updateDescription(modifiers.descriptionExpanded)
            }
            full_info.text = Html.fromHtml(state.info)

            full_days_bar.selectDays(state.days)
            full_days_bar.isVisible = state.isOngoing
            full_days_divider.isVisible = state.isOngoing || state.announce != null

            full_announce.isVisible = state.announce != null
            full_announce.text = state.announce?.let { Html.fromHtml(it) }

            bindFavorite(state.favorite, modifiers.favoriteRefreshing)
        }

        private fun bindFavorite(state: ReleaseFavoriteState, favoritesRefresh: Boolean) {
            full_fav_count.text = state.rating

            val iconRes = if (state.isAdded) R.drawable.ic_fav else R.drawable.ic_fav_border
            full_fav_icon.setCompatDrawable(iconRes)

            full_fav_icon.isVisible = !favoritesRefresh
            full_fav_progress.isVisible = favoritesRefresh

            full_fav_btn.isSelected = state.isAdded
            full_fav_btn.isClickable = !favoritesRefresh
        }

        private fun updateDescription(isExpanded: Boolean) {
            val expanderVisible = full_description.lineCount > full_description.maxLines
            if (isExpanded) {
                full_description.expand()
            } else {
                full_description.collapse()
            }
            full_description_expander.isVisible = expanderVisible
            full_description_expander.text = if (isExpanded) {
                "Скрыть"
            } else {
                "Раскрыть"
            }
        }
    }

    interface Listener {
        fun onClickSomeLink(url: String)

        fun onClickTag(text: String)

        fun onClickFav()

        fun onScheduleClick(day: Int)

        fun onExpandClick()
    }
}