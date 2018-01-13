package ru.radiationx.anilibria.ui.fragments.search

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.ListDelegationAdapter
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.ui.adapters.BaseItemListener
import ru.radiationx.anilibria.ui.adapters.GenreListItem
import ru.radiationx.anilibria.ui.adapters.ListItem
import ru.radiationx.anilibria.ui.adapters.search.GenreItemDelegate

class GenresDialog(context: Context, private val listener: ClickListener) {
    private val dialog: BottomSheetDialog = BottomSheetDialog(context)
    private val adapter: ReleasesAdapter = ReleasesAdapter()
    private val recyclerView: RecyclerView = RecyclerView(context).apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this.context)
        adapter = this@GenresDialog.adapter
    }
    private val items = mutableListOf<GenreItem>()

    fun setItems(items: List<GenreItem>) {
        this.items.apply {
            clear()
            addAll(items)
        }
        adapter.bindItems(items)
        adapter.notifyDataSetChanged()
    }

    fun setChecked(genreValue: String) {
        items.forEachIndexed { index, genreItem ->
            if (genreValue == genreItem.value) {
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(index, 0)
                return@forEachIndexed
            }
        }
        adapter.notifyDataSetChanged()
    }

    fun showDialog() {
        recyclerView.parent?.let {
            (it as ViewGroup).removeView(recyclerView)
        }
        dialog.setContentView(recyclerView)
        dialog.show()
    }

    interface ClickListener {
        fun onItemClick(item: GenreItem)
    }

    inner class ReleasesAdapter : ListDelegationAdapter<MutableList<ListItem>>() {

        private val itemListener = object : BaseItemListener<GenreItem> {
            override fun onItemClick(item: GenreItem, position: Int) {
                setChecked(item.value)
                listener.onItemClick(item)
                dialog.dismiss()
            }

            override fun onItemLongClick(item: GenreItem): Boolean = false
        }

        init {
            items = mutableListOf()
            delegatesManager.run {
                addDelegate(GenreItemDelegate(itemListener))
            }
        }

        fun bindItems(newItems: List<GenreItem>) {
            items.clear()
            items.addAll(newItems.map { GenreListItem(it) })
        }

    }
}
