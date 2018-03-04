package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.ui.adapters.ReleaseListItem
import ru.radiationx.anilibria.ui.adapters.ReleaseRemindListItem
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseRemindDelegate
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter

/**
 * Created by radiationx on 04.03.18.
 */
class SearchAdapter(listener: ItemListener) : ReleasesAdapter(listener) {

    private val remindText = "Если не удаётся найти нужный релиз, попробуйте искать через Google или Yandex c приставкой \"AniLibria\".\nПо ссылке в поисковике можно будет открыть приложение."

    private val remindCloseListener = object : ReleaseRemindDelegate.Listener {
        override fun onClickClose(position: Int) {
            items.removeAt(position)
            notifyItemRangeRemoved(position, 1)
            App.injections.appPreferences.setSearchRemind(false)
        }
    }

    init {
        delegatesManager.run {
            addDelegate(ReleaseRemindDelegate(remindCloseListener))
        }
    }

    override fun bindItems(newItems: List<ReleaseItem>) {
        this.items.clear()
        if (newItems.isEmpty() && App.injections.appPreferences.getSearchRemind()) {
            items.add(ReleaseRemindListItem(remindText))
        }
        this.items.addAll(newItems.map { ReleaseListItem(it) })
        randomInsertVitals()
        addLoadMore()
        notifyDataSetChanged()
    }
}