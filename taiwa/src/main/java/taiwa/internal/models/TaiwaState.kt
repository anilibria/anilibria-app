package taiwa.internal.models

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import envoy.DiffItem
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor


abstract class TaiwaItem(diffId: Any) : DiffItem(diffId)

internal data class TaiwaState(
    val anchor: TaiwaAnchor,
    val header: TaiwaContentState?,
    val body: TaiwaContentState?,
    val footer: TaiwaContentState?,
    val backAction: TaiwaAction?,
    val closeListener: ClickListener?,
)

internal data class TaiwaNestingState(
    val content: TaiwaState,
    val nested: Map<TaiwaAnchor.Id, TaiwaState>,
)

internal data class TaiwaContentState(
    val items: List<DiffItem>
)

internal data class TaiwaToolbarState(
    val id: Any,
    val title: String?,
    val subtitle: String?,
    val withBack: Boolean,
    val withClose: Boolean,
) : DiffItem(id)

internal data class TaiwaMessageState(
    val id: Any,
    val text: String?,
) : DiffItem(id)

internal data class TaiwaItemState(
    val base: Base,
    val type: Type,
) : TaiwaItem(base.id) {

    data class Base(
        val id: Any,
        val title: String?,
        val subtitle: String?,
        @DrawableRes val iconRes: Int?,
        val isEmptyIcon: Boolean,
        @AttrRes val tintAttrRes: Int?,
        val action: TaiwaAction?,
        val clickListener: ClickListener?,
    )

    sealed interface Type {
        data class Basic(val value: String?, val forward: Boolean) : Type
        data class Switch(val selected: Boolean) : Type
        data class Radio(val selected: Boolean) : Type
        data class Checkbox(val selected: Boolean) : Type
    }
}

internal data class TaiwaButtonsState(
    val id: Any,
    val buttons: List<TaiwaButtonState>,
) : DiffItem(id)

internal data class TaiwaButtonState(
    val id: Any,
    val text: String?,
    val action: TaiwaAction?,
    val clickListener: ClickListener?,
)