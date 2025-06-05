package ru.radiationx.anilibria.ui.fragments.search.filter

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import ru.radiationx.anilibria.R
import ru.radiationx.data.api.shared.filter.FilterForm
import taiwa.TaiwaAction
import taiwa.common.DialogType
import taiwa.common.Taiwa

class SearchFilterDialog(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onFormConfirm: (FilterForm) -> Unit,
) {

    private val dialog = Taiwa(context, lifecycleOwner, DialogType.BottomSheet)

    fun show() {
        dialog.show()
    }

    fun setForm(form: FilterForm) {
        dialog.setContent {
            body {
                item {
                    icon(R.drawable.ic_baseline_content_copy_24)
                    title("Копировать ссылку")
                    action(TaiwaAction.Close)
                }
                item {
                    icon(R.drawable.ic_baseline_share_24)
                    title("Поделиться")
                    action(TaiwaAction.Close)
                }
                item {
                    icon(R.drawable.ic_baseline_app_shortcut_24)
                    title("Добавить на главный экран")
                    action(TaiwaAction.Close)
                }
            }
            footer {
                item {
                    icon(R.drawable.ic_baseline_content_copy_24)
                    title("Копировать ссылку")
                    action(TaiwaAction.Close)
                }
            }
        }
    }
}