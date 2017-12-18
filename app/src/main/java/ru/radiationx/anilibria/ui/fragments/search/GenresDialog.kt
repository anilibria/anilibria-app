package ru.radiationx.anilibria.ui.fragments.search

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.data.api.models.GenreItem
import ru.radiationx.anilibria.ui.adapters.BaseAdapter
import ru.radiationx.anilibria.ui.adapters.BaseViewHolder

class GenresDialog(context: Context, private val listener: ClickListener) {
    private val dialog: BottomSheetDialog = BottomSheetDialog(context)
    private val adapter: ReleasesAdapter = ReleasesAdapter()
    private val recyclerView: RecyclerView = RecyclerView(context).apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this.context)
        adapter = this@GenresDialog.adapter
    }
    private var checkedGenre: String = ""

    fun setItems(items: List<GenreItem>) {
        adapter.bindItems(items)
        adapter.notifyDataSetChanged()
    }

    fun setChecked(genreValue: String) {
        checkedGenre = genreValue
        adapter.items.forEachIndexed { index, genreItem ->
            if (checkedGenre == genreItem.value) {
                (recyclerView.layoutManager as LinearLayoutManager)
                        .scrollToPositionWithOffset(index, 0)
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

    inner class ReleasesAdapter : BaseAdapter<GenreItem, ReleasesAdapter.GenreHolder>() {
        override fun onBindViewHolder(holder: ReleasesAdapter.GenreHolder?, position: Int) {
            holder?.bind(items[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReleasesAdapter.GenreHolder {
            return GenreHolder(inflateLayout(parent, R.layout.item_genre))
        }

        inner class GenreHolder internal constructor(itemView: View) : BaseViewHolder<GenreItem>(itemView) {
            private var textView: CheckedTextView = itemView.findViewById<CheckedTextView>(R.id.item_title)

            init {
                itemView.setOnClickListener {
                    val item = items[layoutPosition]
                    setChecked(item.value)
                    listener.onItemClick(items[layoutPosition])
                    dialog.dismiss()
                }
            }

            override fun bind(item: GenreItem) {
                textView.text = item.title
                textView.isChecked = item.value.equals(checkedGenre)
            }
        }
    }
}
