package envoy.recycler

import androidx.recyclerview.widget.DiffUtil
import envoy.DiffItem

class DiffItemAsyncEnvoyAdapter : AsyncEnvoyAdapter<DiffItem>(DiffItemCallback)

class DiffItemEnvoyAdapter : EnvoyAdapter<DiffItem>(DiffItemCallback)

private object DiffItemCallback : DiffUtil.ItemCallback<DiffItem>() {

    override fun areItemsTheSame(oldItem: DiffItem, newItem: DiffItem): Boolean {
        return oldItem.diffId == newItem.diffId
    }

    override fun areContentsTheSame(oldItem: DiffItem, newItem: DiffItem): Boolean {
        return oldItem == newItem
    }
}


