package ru.radiationx.anilibria.ui.common

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.ReleaseItemState
import taiwa.TaiwaAction
import taiwa.common.DialogType
import taiwa.common.Taiwa

fun Fragment.releaseItemDialog(
    onCopyClick: (ReleaseItemState) -> Unit,
    onShareClick: (ReleaseItemState) -> Unit,
    onShortcutClick: (ReleaseItemState) -> Unit,
    onDeleteClick: ((ReleaseItemState) -> Unit)? = null,
): Lazy<ReleaseItemDialog> = lazy {
    ReleaseItemDialog(
        context = requireContext(),
        lifecycleOwner = viewLifecycleOwner,
        onCopyClick = onCopyClick,
        onShareClick = onShareClick,
        onShortcutClick = onShortcutClick,
        onDeleteClick = onDeleteClick
    )
}

class ReleaseItemDialog(
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    private val onCopyClick: (ReleaseItemState) -> Unit,
    private val onShareClick: (ReleaseItemState) -> Unit,
    private val onShortcutClick: (ReleaseItemState) -> Unit,
    private val onDeleteClick: ((ReleaseItemState) -> Unit)? = null,
) {

    private val dialog = Taiwa(context, lifecycleOwner, DialogType.BottomSheet)

    fun show(item: ReleaseItemState) {
        dialog.setContent {
            items {
                action(TaiwaAction.Close)
                item {
                    icon(R.drawable.ic_baseline_content_copy_24)
                    title("Копировать ссылку")
                    onClick {
                        onCopyClick.invoke(item)
                        Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                item {
                    icon(R.drawable.ic_baseline_share_24)
                    title("Поделиться")
                    onClick { onShareClick.invoke(item) }
                }
                item {
                    icon(R.drawable.ic_baseline_app_shortcut_24)
                    title("Добавить на главный экран")
                    onClick { onShortcutClick.invoke(item) }
                }
                if (onDeleteClick != null) {
                    item {
                        icon(R.drawable.ic_baseline_delete_outline_24)
                        tint(androidx.appcompat.R.attr.colorError)
                        title("Удалить")
                        onClick { onDeleteClick.invoke(item) }
                    }
                }
            }
        }
        dialog.show()
    }
}