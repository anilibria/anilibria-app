package ru.radiationx.anilibria.ui.fragments.search

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import ru.radiationx.anilibria.ui.adapters.BaseAdapter
import ru.radiationx.anilibria.ui.adapters.BaseViewHolder

/**
 * Created by mintrocket on 04.12.2017.
 */
class GenresDialog(context: Context, listener: ClickListener) {
    private val dialog: BottomSheetDialog = BottomSheetDialog(context)
    private val listView:ListView = ListView(context)
    private val adapter: ArrayAdapter<String> = ArrayAdapter(context, android.R.layout.select_dialog_item)

    init {
        listView.adapter = adapter
        listView.setOnItemClickListener { adapterView, view, i, l ->
            run {
                dialog.dismiss()
                listener.onItemClick(adapter.getItem(i))
            }
        }
    }

    fun setItems(items:List<String>){
        adapter.addAll(items)
        adapter.notifyDataSetChanged()
    }

    fun showDialog(){
        listView.parent?.let {
            (it as ViewGroup).removeView(listView)
        }
        dialog.setContentView(listView)
        dialog.show()
    }

    interface ClickListener{
        fun onItemClick(item: String)
    }

    /*class ReleasesAdapter : BaseAdapter<String, BaseViewHolder<*>>(){
        override fun onBindViewHolder(holder: BaseViewHolder<*>?, position: Int) {

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<*> {

        }

        private inner class LoadMoreHolder internal constructor(itemView: View) : BaseViewHolder<String>(itemView) {


            override fun bind(position: Int) {
            }
        }

    }*/
}
