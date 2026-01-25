package taiwa.internal.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import envoy.ext.viewBindingEnvoy
import taiwa.databinding.TaiwaItemBinding
import taiwa.internal.models.TaiwaItemState

internal fun itemEnvoy(
    clickListener: (TaiwaItemState) -> Unit
) = viewBindingEnvoy<TaiwaItemState, TaiwaItemBinding> {

    val defaultIconTint = ImageViewCompat.getImageTintList(view.itemIcon)

    val defaultTitleColor = view.itemTitle.textColors

    view.itemSwitch.isClickable = false
    view.itemRadio.isClickable = false
    view.itemCheckbox.isClickable = false

    bind { item ->
        val base = item.base
        val basicType = item.type as? TaiwaItemState.Type.Basic
        val switchType = item.type as? TaiwaItemState.Type.Switch
        val radioType = item.type as? TaiwaItemState.Type.Radio
        val checkboxType = item.type as? TaiwaItemState.Type.Checkbox

        val itemTint = base.tintAttrRes?.let {
            view.root.context.getColorFromAttr(it)
        }

        view.itemIcon.visibility = when {
            base.iconRes != null -> View.VISIBLE
            base.isEmptyIcon -> View.INVISIBLE
            else -> View.GONE
        }
        if (base.iconRes != null) {
            view.itemIcon.setImageResource(base.iconRes)
        } else {
            view.itemIcon.setImageDrawable(null)
        }
        ImageViewCompat.setImageTintList(view.itemIcon, itemTint ?: defaultIconTint)

        view.itemTitle.setStateText(base.title)
        view.itemSubtitle.setStateText(base.subtitle)
        view.itemTitle.setTextColor(itemTint ?: defaultTitleColor)

        view.itemValue.setStateText(basicType?.value)
        view.itemNavIcon.isVisible = basicType?.forward == true

        view.itemSwitch.isVisible = switchType != null
        view.itemSwitch.isChecked = switchType?.selected == true

        view.itemRadio.isVisible = radioType != null
        view.itemRadio.isChecked = radioType?.selected == true

        view.itemCheckbox.isVisible = checkboxType != null
        view.itemCheckbox.isChecked = checkboxType?.selected == true

        view.root.setOnClickListener {
            clickListener.invoke(item)
        }
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