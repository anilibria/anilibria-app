package ru.radiationx.anilibria.ui.adapters.release.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_episode_control.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseEpisodeControlItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.data.entity.app.release.ReleaseFull
import ru.radiationx.shared.ktx.android.visible

/**
 * Created by radiationx on 13.01.18.
 */
class ReleaseEpisodeControlDelegate(
        private val itemListener: Listener
) : AppAdapterDelegate<ReleaseEpisodeControlItem, ListItem, ReleaseEpisodeControlDelegate.ViewHolder>(
        R.layout.item_release_episode_control,
        { it is ReleaseEpisodeControlItem },
        { ViewHolder(it, itemListener) }
) {

    override fun bindData(item: ReleaseEpisodeControlItem, holder: ViewHolder) =
            holder.bind(item.item, item.hasWeb)

    class ViewHolder(
            override val containerView: View,
            private val itemListener: Listener
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            full_btn_episodes_menu.setOnClickListener { itemListener.onClickEpisodesMenu() }
            full_button_web.setOnClickListener { itemListener.onClickWatchWeb() }
        }

        fun bind(item: ReleaseFull, hasWeb: Boolean) {
            val hasEpisodes = !item.episodes.isEmpty()
            val hasViewed = item.episodes.firstOrNull { it.isViewed } != null
            full_button_continue.isEnabled = hasEpisodes
            full_button_web.visible(hasWeb)

            val lastViewed = item.episodes.asReversed().maxBy { it.lastAccess }

            if (hasViewed) {
                full_button_continue.text = "Продолжить c ${lastViewed?.id} серии"
                full_button_continue.setOnClickListener { itemListener.onClickContinue() }
            } else {
                full_button_continue.text = "Начать просмотр"
                full_button_continue.setOnClickListener { itemListener.onClickWatchAll() }
            }
        }
    }

    interface Listener {

        fun onClickWatchWeb()

        fun onClickWatchAll()

        fun onClickContinue()

        fun onClickEpisodesMenu()
    }
}