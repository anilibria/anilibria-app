package ru.radiationx.anilibria.ui.adapters.release.detail

import android.graphics.PorterDuff
import android.support.design.chip.Chip
import android.support.design.chip.ChipGroup
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import at.blogc.android.views.ExpandableTextView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_head_new.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.entity.app.schedule.ScheduleDay
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.extension.setCompatDrawable
import ru.radiationx.anilibria.extension.visible
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseHeadListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.utils.LinkMovementMethod

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

        private var tagColor = 0
        private var tagColorPress = 0
        private var tagColorText = 0
        private var tagRadius = 0f

        private lateinit var currentItem: ReleaseFull

        init {
            containerView.context.let {
                tagColor = it.getColorFromAttr(R.attr.release_tag_color)
                tagColorPress = it.getColorFromAttr(R.attr.release_tag_color_press)
                tagColorText = it.getColorFromAttr(R.attr.textColoredButton)
                tagRadius = 2 * it.resources.displayMetrics.density
            }

            full_button_torrent.setOnClickListener {
                itemListener.onClickTorrent()
            }
            /*full_tags.setOnTagClickListener { tag, _ ->
                itemListener.onClickTag(tag.text)
            }*/
            full_button_watch_web.setOnClickListener {
                itemListener.onClickWatchWeb()
            }
            full_fav_btn.setOnClickListener {
                itemListener.onClickFav()
            }

            full_days_bar.clickListener = {
                itemListener.onScheduleClick(it)
            }
        }

        fun bind(item: ReleaseFull) {
            currentItem = item
            full_title.text = item.title
            full_description.text = item.description?.let { Html.fromHtml(it) }

            full_description.movementMethod = LinkMovementMethod { itemListener.onClickSomeLink(it) }

            full_description.post {
                updateDescription()
            }
            full_description.addOnExpandListener(object : ExpandableTextView.SimpleOnExpandListener() {
                override fun onExpand(view: ExpandableTextView) {
                    super.onExpand(view)
                    updateDescription(true)
                }

                override fun onCollapse(view: ExpandableTextView) {
                    super.onCollapse(view)
                    updateDescription(false)
                }
            })
            full_description_expander.setOnClickListener {
                full_description.toggle()
            }

            full_button_torrent.isEnabled = !item.torrents.isEmpty()

            //updateGenres(item.genres)

            val seasonsHtml = "<b>Год:</b> " + item.seasons.joinToString(", ")
            val voicesHtml = "<b>Голоса:</b> " + item.voices.joinToString(", ")
            val typesHtml = "<b>Тип:</b> " + item.types.joinToString(", ")
            val releaseStatus = item.status ?: "Не указано"
            val releaseStatusHtml = "<b>Состояние релиза:</b> $releaseStatus"
            val genresHtml = "<b>Жанр:</b> " + item.genres.joinToString(", ") { "<a href=\"#\">${it.capitalize()}</a>" }
            val arrHtml = arrayOf(
                    item.titleEng,
                    seasonsHtml,
                    voicesHtml,
                    typesHtml,
                    releaseStatusHtml,
                    genresHtml
            )
            full_info.text = Html.fromHtml(arrHtml.joinToString("<br>"))

            val hasMoonwalk = item.moonwalkLink != null
            //full_button_watch_all.isEnabled = hasEpisodes
            full_button_watch_web.isEnabled = hasMoonwalk

            //full_button_watch_all.visibility = if (hasEpisodes || hasMoonwalk) View.VISIBLE else View.GONE

            full_days_bar.selectDays(item.days.map { ScheduleDay.toCalendarDay(it) })
            full_days_bar.visible(item.statusCode == ReleaseItem.STATUS_CODE_PROGRESS)
            full_days_divider.visible(item.statusCode == ReleaseItem.STATUS_CODE_PROGRESS || item.announce != null)

            full_announce.visible(item.announce != null)
            full_announce.text = item.announce?.let { Html.fromHtml(it) }

            item.favoriteInfo.let {
                full_fav_count.text = it.rating.toString()

                val iconRes = if (it.isAdded) R.drawable.ic_fav else R.drawable.ic_fav_border
                full_fav_icon.setCompatDrawable(iconRes)

                full_fav_icon.visible(!it.inProgress)
                full_fav_progress.visible(it.inProgress)

                full_fav_btn.isSelected = it.isAdded
                full_fav_btn.isClickable = !it.inProgress
            }

        }

        fun updateDescription(isExpanded: Boolean = false) {
            full_description?.also {
                full_description_expander.visible(it.lineCount > it.maxLines)
                full_description_expander.text = if (isExpanded) {
                    "Скрыть"
                } else {
                    "Раскрыть"
                }
            }
        }

        private fun updateGenres(genres: List<String>) {
            full_tags.removeAllViews()
            genres.forEach { genre ->
                val chip = Chip(full_tags.context).also {
                    it.text = genre
                    it.setTextColor(it.context.getColorFromAttr(R.attr.textDefault))
                    //it.setChipBackgroundColorResource(R.color.bg_chip)
                    //it.setOnCheckedChangeListener(yearsChipListener)
                    it.layoutParams = ChipGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        bottomMargin = 100
                    }
                    it.setOnClickListener { itemListener.onClickTag((it as Chip).text.toString()) }
                }
                full_tags.addView(chip)
            }
        }
    }

    interface Listener {
        fun onClickSomeLink(url: String): Boolean

        fun onClickTorrent()

        fun onClickTag(text: String)

        fun onClickWatchWeb()

        fun onClickFav()

        fun onScheduleClick(day: Int)
    }
}