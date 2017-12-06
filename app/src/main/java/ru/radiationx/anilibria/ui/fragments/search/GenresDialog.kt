package ru.radiationx.anilibria.ui.fragments.search

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.radiationx.anilibria.ui.adapters.BaseAdapter
import ru.radiationx.anilibria.ui.adapters.BaseViewHolder

/**
 * Created by mintrocket on 04.12.2017.
 */
class GenresDialog(context: Context, private val listener: ClickListener) {
    private val dialog: BottomSheetDialog = BottomSheetDialog(context)
    private val adapter: ReleasesAdapter = ReleasesAdapter()
    private val recyclerView: RecyclerView = RecyclerView(context).apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this.context)
        adapter = adapter
    }

    fun setItems(items: List<String>) {
        adapter.bindItems(items)
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
        fun onItemClick(item: String)
    }

    inner class ReleasesAdapter : BaseAdapter<String, ReleasesAdapter.GenreHolder>() {
        override fun onBindViewHolder(holder: ReleasesAdapter.GenreHolder?, position: Int) {
            holder?.bind(items[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ReleasesAdapter.GenreHolder {
            return GenreHolder(inflateLayout(parent, android.R.layout.simple_selectable_list_item))
        }

        inner class GenreHolder internal constructor(itemView: View) : BaseViewHolder<String>(itemView) {
            init {
                itemView.setOnClickListener {
                    listener.onItemClick(items[layoutPosition])
                }
            }

            override fun bind(item: String) {
                itemView.findViewById<TextView>(android.R.id.text1).text = item
            }
        }
    }
}
