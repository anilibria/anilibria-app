package ru.radiationx.anilibria.ui.adapters.release.detail

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cunoraz.tagview.Tag
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.synthetic.main.item_release_head_new.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.extension.getColorFromAttr
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseHeadListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate
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
            val view: View,
            private val itemListener: Listener
    ) : RecyclerView.ViewHolder(view) {
        private var tagColor = 0
        private var tagColorPress = 0
        private var tagColorText = 0
        private var tagRadius = 0f

        private lateinit var currentItem: ReleaseFull

        init {
            view.context.let {
                tagColor = it.getColorFromAttr(R.attr.release_tag_color)
                tagColorPress = it.getColorFromAttr(R.attr.release_tag_color_press)
                tagColorText = it.getColorFromAttr(R.attr.textColoredButton)
                tagRadius = 2 * it.resources.displayMetrics.density
            }

            view.run {
                full_button_torrent.setOnClickListener {
                    itemListener.onClickTorrent()
                }
                full_tags.setOnTagClickListener { tag, _ ->
                    itemListener.onClickTag(tag.text)
                }
                full_button_watch_web.setOnClickListener {
                    itemListener.onClickWatchWeb()
                }
                full_fav_btn.setOnClickListener {
                    itemListener.onClickFav()
                }
            }
        }

        fun bind(item: ReleaseFull) {
            currentItem = item
            view.run {
                full_title.text = item.title
                full_description.text = Html.fromHtml(item.description)

                full_description.movementMethod = LinkMovementMethod { itemListener.onClickSomeLink(it) }

                full_button_torrent.isEnabled = !item.torrents.isEmpty()

                if (full_tags.tags.isEmpty()) {
                    item.genres.forEach {
                        val tag = Tag(it)
                        tag.layoutColor = tagColor
                        tag.layoutColorPress = tagColorPress
                        tag.tagTextColor = tagColorText
                        tag.radius = tagRadius
                        full_tags.addTag(tag)
                    }
                }

                val seasonsHtml = "<b>Год:</b> " + item.seasons.joinToString(", ")
                val voicesHtml = "<b>Голоса:</b> " + item.voices.joinToString(", ")
                val typesHtml = "<b>Тип:</b> " + item.types.joinToString(", ")
                val releaseStatus = item.status ?: "Не указано"
                val releaseStatusHtml = "<b>Состояние релиза:</b> $releaseStatus"
                val arrHtml = arrayOf(
                        item.titleEng,
                        seasonsHtml,
                        voicesHtml,
                        typesHtml,
                        releaseStatusHtml
                )
                full_info.text = Html.fromHtml(arrHtml.joinToString("<br>"))

                val hasMoonwalk = item.moonwalkLink != null
                //full_button_watch_all.isEnabled = hasEpisodes
                full_button_watch_web.isEnabled = hasMoonwalk

                //full_button_watch_all.visibility = if (hasEpisodes || hasMoonwalk) View.VISIBLE else View.GONE

                item.favoriteInfo.let {
                    full_fav_count.text = it.rating.toString()
                    val iconRes = if (it.isAdded) R.drawable.ic_fav else R.drawable.ic_fav_border
                    full_fav_icon.setImageDrawable(ContextCompat.getDrawable(full_fav_icon.context, iconRes))
                    if (it.isAdded && !it.inProgress) {
                        full_fav_btn.background.setColorFilter(context.getColorFromAttr(R.attr.colorAccent), PorterDuff.Mode.SRC_ATOP)
                    } else {
                        full_fav_btn.background.clearColorFilter()
                    }
                    full_fav_btn.isClickable = /*!it.isGuest && */!it.inProgress
                }

            }
        }
    }

    interface Listener {
        fun onClickSomeLink(url: String): Boolean

        fun onClickTorrent()

        fun onClickTag(text: String)

        fun onClickWatchWeb()

        fun onClickFav()
    }
}