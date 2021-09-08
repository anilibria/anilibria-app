package ru.radiationx.anilibria.ui.fragments.search

import ru.radiationx.anilibria.ui.adapters.*
import ru.radiationx.anilibria.ui.adapters.global.LoadMoreDelegate
import ru.radiationx.anilibria.ui.adapters.release.detail.ReleaseRemindDelegate
import ru.radiationx.anilibria.ui.adapters.release.list.ReleaseItemDelegate
import ru.radiationx.anilibria.ui.common.adapters.ListItemAdapter
import ru.radiationx.anilibria.ui.fragments.release.list.ReleasesAdapter
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.shared_app.di.DI

/**
 * Created by radiationx on 04.03.18.
 */
class SearchAdapter(
    listener: ReleasesAdapter.ItemListener,
    private val placeholder: PlaceholderListItem
) : ListItemAdapter() {

    private val remindText =
        "Если не удаётся найти нужный релиз, попробуйте искать через Google или Yandex c приставкой \"AniLibria\".\nПо ссылке в поисковике можно будет открыть приложение."

    private val appPreferences = DI.get(PreferencesHolder::class.java)

    private val remindCloseListener = object : ReleaseRemindDelegate.Listener {
        override fun onClickClose(position: Int) {
            localItems.removeAt(position)
            notifyDiffItems()
            appPreferences.setSearchRemind(false)
        }
    }

    init {
        delegatesManager.run {
            addDelegate(ReleaseRemindDelegate(remindCloseListener))
            addDelegate(ReleaseItemDelegate(listener))
            addDelegate(LoadMoreDelegate(listener))
            addDelegate(PlaceholderDelegate())
        }
    }

    fun bindState(state: SearchScreenState) {
        val newItems = mutableListOf<ListItem>()
        if (state.items.isEmpty() && !state.refreshing && state.remindText != null) {
            newItems.add(ReleaseRemindListItem(state.remindText))
        }
        if (state.items.isEmpty() && !state.refreshing) {
            newItems.add(placeholder)
        }
        newItems.addAll(state.items.map { ReleaseListItem(it) })
        if (state.hasMorePages) {
            newItems.add(LoadMoreListItem("bottom"))
        }
        items = newItems
    }
}