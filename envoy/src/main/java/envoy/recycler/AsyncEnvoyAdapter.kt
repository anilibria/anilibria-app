package envoy.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import envoy.Envoy
import envoy.EnvoyManager
import envoy.EnvoyPresenter

open class AsyncEnvoyAdapter<T> : ListAdapter<T, AsyncEnvoyAdapter.ViewHolder<T>> {

    constructor(diffCallback: DiffUtil.ItemCallback<T>) : super(diffCallback)

    constructor(config: AsyncDifferConfig<T>) : super(config)

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
}