package envoy.recycler

import androidx.recyclerview.widget.DiffUtil
import envoy.DiffItem

class DiffItemEnvoyAdapter : EnvoyAdapter<DiffItem>(DiffItemCallback)

private object DiffItemCallback : DiffUtil.ItemCallback<DiffItem>() {

    override fun areItemsTheSame(oldItem: DiffItem, newItem: DiffItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DiffItem, newItem: DiffItem): Boolean {
        return oldItem == newItem
    }
}