package ru.radiationx.anilibria.ui.adapters.release.detail

import android.text.Html
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import at.blogc.android.views.ExpandableTextView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_head_new.*
import ru.radiationx.anilibria.R
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

    override fun bindData(item: ReleaseHeadListItem, holder: ViewHolder) = holder.bind(item.item)

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

            full_description.addOnExpandListener(object :
                ExpandableTextView.SimpleOnExpandListener() {
                override fun onExpand(view: ExpandableTextView) {
                    super.onExpand(view)
                    updateDescription(true)
                }

                override fun onCollapse(view: ExpandableTextView) {
                    super.onCollapse(view)
                    updateDescription(false)
                }
            })
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
                full_description.toggle()
            }
        }

        fun bind(state: ReleaseInfoState) {
            full_title.text = state.titleRus
            full_title_en.text = state.titleEng
            full_description.text = Html.fromHtml(state.description)
            full_description.doOnLayout {
                updateDescription()
            }
            full_info.text = Html.fromHtml(state.info)

            full_days_bar.selectDays(state.days)
            full_days_bar.isVisible = state.isOngoing
            full_days_divider.isVisible = state.isOngoing || state.announce != null

            full_announce.isVisible = state.announce != null
            full_announce.text = state.announce?.let { Html.fromHtml(it) }

            bindFavorite(state.favorite)
        }

        private fun bindFavorite(state: ReleaseFavoriteState) {
            full_fav_count.text = state.rating

            val iconRes = if (state.isAdded) R.drawable.ic_fav else R.drawable.ic_fav_border
            full_fav_icon.setCompatDrawable(iconRes)

            full_fav_icon.isVisible = !state.isRefreshing
            full_fav_progress.isVisible = (state.isRefreshing)

            full_fav_btn.isSelected = state.isAdded
            full_fav_btn.isClickable = !state.isRefreshing
        }

        private fun updateDescription(isExpanded: Boolean? = null) {
            val newExpanded = isExpanded ?: full_description.isExpanded
            val expanderVisible = full_description.lineCount > full_description.maxLines
            itemListener.onExpandStateChanged(newExpanded)
            full_description_expander.isVisible = expanderVisible
            full_description_expander.text = if (newExpanded) {
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

        fun onExpandStateChanged(isExpanded: Boolean)
    }
}