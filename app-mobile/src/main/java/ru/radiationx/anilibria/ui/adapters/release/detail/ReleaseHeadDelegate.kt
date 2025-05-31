package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.core.text.parseAsHtml
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import dev.androidbroadcast.vbpd.viewBinding
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.databinding.ItemReleaseHeadNewBinding
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseHeadListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseDetailModifiersState
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseFavoriteState
import ru.radiationx.anilibria.ui.fragments.release.details.ReleaseInfoState
import ru.radiationx.anilibria.utils.LinkMovementMethod
import ru.radiationx.anilibria.utils.dimensions.Side
import ru.radiationx.anilibria.utils.dimensions.dimensionsApplier
import ru.radiationx.data.apinext.models.enums.PublishDay
import ru.radiationx.shared.ktx.android.relativeDate
import ru.radiationx.shared.ktx.android.setCompatDrawable

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseHeadDelegate(
    private val itemListener: Listener,
) : AppAdapterDelegate<ReleaseHeadListItem, ListItem, ReleaseHeadDelegate.ViewHolder>(
    R.layout.item_release_head_new,
    { it is ReleaseHeadListItem },
    { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: ReleaseHeadListItem, holder: ViewHolder) =
        holder.bind(item.item, item.modifiers)

    class ViewHolder(
        itemView: View,
        private val itemListener: Listener,
    ) : RecyclerView.ViewHolder(itemView) {

        private val binding by viewBinding<ItemReleaseHeadNewBinding>()

        private val dimensionsApplier by dimensionsApplier()

        init {
            val tagsRegex = Regex("(\\w+?)_([\\s\\S]+)")

            binding.fullFavBtn.setOnClickListener {
                itemListener.onClickFav()
            }
            binding.fullDaysBar.clickListener = {
                itemListener.onScheduleClick(it)
            }
            binding.fullInfo.movementMethod = LinkMovementMethod {
                val match = tagsRegex.find(it) ?: return@LinkMovementMethod true
                val tag = match.groupValues[1]
                val value = match.groupValues[2]
                itemListener.onClickGenre(tag, value)
                true
            }
            binding.fullAnnounce.movementMethod = LinkMovementMethod {
                itemListener.onClickSomeLink(it)
                true
            }
            binding.fullDescription.setAnimationDuration(225)
            binding.fullDescription.movementMethod = LinkMovementMethod {
                itemListener.onClickSomeLink(it)
                true
            }
            binding.fullDescriptionExpander.setOnClickListener {
                itemListener.onExpandClick()
            }
        }

        fun bind(state: ReleaseInfoState, modifiers: ReleaseDetailModifiersState) {
            dimensionsApplier.applyPaddings(Side.Left, Side.Right)
            binding.fullTitle.text = state.titleRus
            binding.fullTitleEn.text = state.titleEng
            binding.fullUpdated.isVisible = state.freshAt != null
            state.freshAt?.also { updatedAt ->
                binding.fullUpdated.text = updatedAt
                    .relativeDate(binding.fullUpdated.context)
                    .let { "Обновлён $it" }
            }

            binding.fullDescription.text = state.description
            binding.fullDescription.doOnLayout {
                updateDescription(modifiers.descriptionExpanded)
            }
            binding.fullInfo.text = state.info.parseAsHtml()

            binding.fullDaysBar.selectDay(state.publishDay)
            binding.fullDaysBar.isVisible = state.needShowDay
            binding.fullDaysDivider.isVisible = state.needShowDay || state.announce != null

            binding.fullAnnounce.isVisible = state.announce != null
            binding.fullAnnounce.text = state.announce

            bindFavorite(
                state.favorite,
                modifiers.favoriteRefreshing || modifiers.favoriteLoading || modifiers.detailLoading
            )
        }

        private fun bindFavorite(state: ReleaseFavoriteState, favoritesRefresh: Boolean) {
            binding.fullFavCount.text = state.rating

            val iconRes = if (state.isAdded) R.drawable.ic_fav else R.drawable.ic_fav_border
            binding.fullFavIcon.setCompatDrawable(iconRes)

            binding.fullFavIcon.isVisible = !favoritesRefresh
            binding.fullFavProgress.isVisible = favoritesRefresh

            binding.fullFavBtn.isSelected = state.isAdded
            binding.fullFavBtn.isClickable = !favoritesRefresh
        }

        private fun updateDescription(isExpanded: Boolean) {
            val expanderVisible =
                binding.fullDescription.lineCount > binding.fullDescription.maxLines
            if (isExpanded) {
                binding.fullDescription.expand()
            } else {
                binding.fullDescription.collapse()
            }
            binding.fullDescriptionExpander.isVisible = expanderVisible
            binding.fullDescriptionExpander.text = if (isExpanded) {
                "Скрыть"
            } else {
                "Раскрыть"
            }
        }
    }

    interface Listener {
        fun onClickSomeLink(url: String)

        fun onClickGenre(tag: String, value: String)

        fun onClickFav()

        fun onScheduleClick(day: PublishDay)

        fun onExpandClick()
    }
}