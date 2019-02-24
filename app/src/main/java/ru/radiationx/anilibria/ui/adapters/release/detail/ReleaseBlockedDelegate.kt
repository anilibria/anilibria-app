package ru.radiationx.anilibria.ui.adapters.release.detail

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_release_blocked.*
import kotlinx.android.synthetic.main.item_release_blocked.view.*
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseBlockedListItem
import ru.radiationx.anilibria.ui.common.adapters.AppAdapterDelegate
import ru.radiationx.anilibria.ui.common.adapters.OptimizeDelegate

/**
 * Created by radiationx on 21.01.18.
 */
class ReleaseBlockedDelegate : AppAdapterDelegate<ReleaseBlockedListItem, ListItem, ReleaseBlockedDelegate.ViewHolder>(
        R.layout.item_release_blocked,
        { it is ReleaseBlockedListItem },
        { ViewHolder(it) }
) {

    override fun bindData(item: ReleaseBlockedListItem, holder: ViewHolder) = holder.bind(item.item)

    class ViewHolder(
            override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: ReleaseFull) {
            val defaultReason = """
                    <h4>Контент недоступен на территории Российской Федерации*. Приносим извинения за неудобства.</h4>
                    <br>
                    <span>Подробности смотрите в новостях или социальных сетях</span>""".trimIndent()
            item_title.text = Html.fromHtml(item.blockedInfo.reason ?: defaultReason)
        }
    }
}