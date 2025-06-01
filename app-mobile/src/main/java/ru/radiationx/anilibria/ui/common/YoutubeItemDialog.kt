package ru.radiationx.anilibria.ui.common

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.model.YoutubeItemState
import taiwa.TaiwaAction
import taiwa.common.DialogType
import taiwa.common.Taiwa

fun Fragment.youtubeItemDialog(
    onCopyClick: (YoutubeItemState) -> Unit,
    onShareClick: (YoutubeItemState) -> Unit,
): Lazy<YoutubeItemDialog> = lazy {
    YoutubeItemDialog(
        context = requireContext(),
        lifecycleOwner = viewLifecycleOwner,
        onCopyClick = onCopyClick,
        onShareClick = onShareClick,
    )
}

class YoutubeItemDialog(
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    private val onCopyClick: (YoutubeItemState) -> Unit,
    private val onShareClick: (YoutubeItemState) -> Unit,
) {

    private val dialog = Taiwa(context, lifecycleOwner, DialogType.BottomSheet)

    fun show(item: YoutubeItemState) {
        dialog.setContent {
            body {
                item {
                    icon(R.drawable.ic_baseline_content_copy_24)
                    title("Копировать ссылку")
                    action(TaiwaAction.Close)
                    onClick {
                        onCopyClick.invoke(item)
                        Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                item {
                    icon(R.drawable.ic_baseline_share_24)
                    title("Поделиться")
                    action(TaiwaAction.Close)
                    onClick { onShareClick.invoke(item) }
                }
            }
        }
        dialog.show()
    }
}