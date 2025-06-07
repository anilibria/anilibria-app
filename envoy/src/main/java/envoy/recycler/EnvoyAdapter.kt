package envoy.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import envoy.Envoy
import envoy.EnvoyManager
import envoy.EnvoyPresenter

open class EnvoyAdapter<T : Any>(
    private val diffCallback: DiffUtil.ItemCallback<T>
) : RecyclerView.Adapter<EnvoyAdapter.ViewHolder<T>>() {

    private var items = emptyList<T>()

    private val manager = EnvoyManager<T>()

    fun addEnvoy(envoy: Envoy<T>) {
        manager.addEnvoy(envoy)
    }

    fun removeEnvoy(envoy: Envoy<T>) {
        manager.removeEnvoy(envoy)
    }

    fun clearEnvoy() {
        manager.clear()
    }

    fun setItems(newItems: List<T>) {
        val callback = createCallBack(items, newItems, diffCallback)
        val diffResult = DiffUtil.calculateDiff(callback, true)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    fun getItem(position: Int): T {
        return items[position]
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return manager.getItemViewType(item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<T> {
        val envoy = manager.getByViewType(viewType)
        val presenter = envoy.onCreate(parent)
        return ViewHolder(presenter)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val envoy = manager.getByViewType(holder.itemViewType)
        val item = getItem(position)
        envoy.onBind(holder.presenter, item)
    }

    override fun onViewAttachedToWindow(holder: ViewHolder<T>) {
        val envoy = manager.getByViewType(holder.itemViewType)
        envoy.onAttach(holder.presenter)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder<T>) {
        val envoy = manager.getByViewType(holder.itemViewType)
        envoy.onDetach(holder.presenter)
    }

    class ViewHolder<T>(
        val presenter: EnvoyPresenter<T>
    ) : RecyclerView.ViewHolder(presenter.view)

    fun createCallBack(
        oldList: List<T>,
        newList: List<T>,
        itemCallback: DiffUtil.ItemCallback<T>
    ): DiffUtil.Callback = object : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return itemCallback.areItemsTheSame(
                oldList[oldItemPosition],
                newList[newItemPosition]
            )
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return itemCallback.areContentsTheSame(
                oldList[oldItemPosition],
                newList[newItemPosition]
            )
        }
    }
}