package ru.radiationx.anilibria.ui.adapters.release.detail

import android.graphics.Color
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
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseHeadListItem
import ru.radiationx.anilibria.utils.LinkMovementMethod

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseHeadDelegate(private val itemListener: Listener) : AdapterDelegate<MutableList<ListItem>>() {
    override fun isForViewType(items: MutableList<ListItem>, position: Int): Boolean = items[position] is ReleaseHeadListItem

    override fun onBindViewHolder(items: MutableList<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val item = items[position] as ReleaseHeadListItem
        (holder as ViewHolder).bind(item.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_release_head_new, parent, false)
    )

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private var tagColor = 0
        private var tagColorPress = 0
        private var tagColorText = 0
        private var tagRadius = 0f

        private lateinit var currentItem: ReleaseFull

        init {
            view.context.let {
                tagColor = ContextCompat.getColor(it, R.color.release_tag_color)
                tagColorPress = ContextCompat.getColor(it, R.color.release_tag_color_press)
                tagColorText = ContextCompat.getColor(it, R.color.white)
                tagRadius = 2 * it.resources.displayMetrics.density
            }

            view.run {
                full_button_torrent.setOnClickListener {
                    itemListener.onClickTorrent(currentItem.torrentLink)
                }
                full_tags.setOnTagClickListener { tag, i ->
                    itemListener.onClickTag(tag.text)
                }
                full_button_watch_all.setOnClickListener {
                    itemListener.onClickWatchAll()
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

                full_description.movementMethod = LinkMovementMethod({ itemListener.onClickSomeLink(it) })

                full_button_torrent.isEnabled = !item.torrentLink.isNullOrEmpty() || !item.torrents.isEmpty()

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

                val seasonsHtml = "<b>Сезон:</b> " + item.seasons.joinToString(", ")
                val voicesHtml = "<b>Голоса:</b> " + item.voices.joinToString(", ")
                val typesHtml = "<b>Тип:</b> " + item.types.joinToString(", ")
                val releaseStatus = item.releaseStatus ?: "Не указано"
                val releaseStatusHtml = "<b>Состояние релиза:</b> $releaseStatus"
                val arrHtml = arrayOf(
                        item.originalTitle,
                        seasonsHtml,
                        voicesHtml,
                        typesHtml,
                        releaseStatusHtml
                )
                full_info.text = Html.fromHtml(arrHtml.joinToString("<br>"))

                val hasEpisodes = !item.episodes.isEmpty()
                val hasMoonwalk = item.moonwalkLink != null
                full_button_watch_all.isEnabled = hasEpisodes || hasMoonwalk

                //full_button_watch_all.visibility = if (hasEpisodes || hasMoonwalk) View.VISIBLE else View.GONE

                item.favoriteCount.let {
                    full_fav_count.text = it.count.toString()
                    val iconRes = if (it.isFaved) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                    full_fav_icon.setImageDrawable(ContextCompat.getDrawable(full_fav_icon.context, iconRes))
                    if (it.isFaved && !it.inProgress) {
                        full_fav_btn.background.setColorFilter(Color.parseColor("#c40304"), PorterDuff.Mode.SRC_ATOP)
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

        fun onClickTorrent(url: String?)

        fun onClickTag(text: String)

        fun onClickWatchAll()

        fun onClickFav()
    }
}