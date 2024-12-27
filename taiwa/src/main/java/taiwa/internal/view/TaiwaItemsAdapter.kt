package taiwa.internal.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import taiwa.R
import taiwa.internal.models.TaiwaItemState
import taiwa.internal.models.TaiwaItemTypeState
import taiwa.databinding.TaiwaItemBinding

internal class TaiwaItemsAdapter(
    private val clickListener: (TaiwaItemState) -> Unit,
) : ListAdapter<TaiwaItemState, TaiwaItemViewHolder>(ItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaiwaItemViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.taiwa_item, parent, false)
        return TaiwaItemViewHolder(itemView, clickListener)
    }

    override fun onBindViewHolder(holder: TaiwaItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}


internal class TaiwaItemViewHolder(
    itemView: View,
    private val clickListener: (TaiwaItemState) -> Unit,
) : RecyclerView.ViewHolder(itemView) {

    private val binding by viewBinding<TaiwaItemBinding>()

    private val defaultIconTint = ImageViewCompat.getImageTintList(binding.itemIcon)

    private val defaultTitleColor = binding.itemTitle.textColors

    init {
        binding.itemSwitch.isClickable = false
        binding.itemRadio.isClickable = false
        binding.itemCheckbox.isClickable = false
    }

    fun bind(item: TaiwaItemState) {
        val base = item.base
        val basicType = item.type as? TaiwaItemTypeState.Basic
        val switchType = item.type as? TaiwaItemTypeState.Switch
        val radioType = item.type as? TaiwaItemTypeState.Radio
        val checkboxType = item.type as? TaiwaItemTypeState.Checkbox

        val itemTint = base.tintAttrRes?.let {
            binding.root.context.getColorFromAttr(it)
        }

        binding.itemIcon.visibility = when {
            base.iconRes != null -> View.VISIBLE
            base.isEmptyIcon -> View.INVISIBLE
            else -> View.GONE
        }
        if (base.iconRes != null) {
            binding.itemIcon.setImageResource(base.iconRes)
        } else {
            binding.itemIcon.setImageDrawable(null)
        }
        ImageViewCompat.setImageTintList(binding.itemIcon, itemTint ?: defaultIconTint)

        binding.itemTitle.setStateText(base.title)
        binding.itemSubtitle.setStateText(base.subtitle)
        binding.itemTitle.setTextColor(itemTint ?: defaultTitleColor)

        binding.itemValue.setStateText(basicType?.value)
        binding.itemNavIcon.isVisible = basicType?.forward == true

        binding.itemSwitch.isVisible = switchType != null
        binding.itemSwitch.isChecked = switchType?.selected == true

        binding.itemRadio.isVisible = radioType != null
        binding.itemRadio.isChecked = radioType?.selected == true

        binding.itemCheckbox.isVisible = checkboxType != null
        binding.itemCheckbox.isChecked = checkboxType?.selected == true

        binding.root.setOnClickListener {
            clickListener.invoke(item)
        }
    }

    private fun Context.getColorFromAttr(@AttrRes attr: Int): ColorStateList? {
        val typedValue = TypedValue()
        return if (theme.resolveAttribute(attr, typedValue, true)) {
            ContextCompat.getColorStateList(this, typedValue.resourceId)
        } else {
            null
        }
    }
}

private object ItemDiffCallback : DiffUtil.ItemCallback<TaiwaItemState>() {

    override fun areItemsTheSame(oldItem: TaiwaItemState, newItem: TaiwaItemState): Boolean {
        return oldItem.base.id == newItem.base.id
    }

    override fun areContentsTheSame(oldItem: TaiwaItemState, newItem: TaiwaItemState): Boolean {
        return oldItem == newItem
    }
}
