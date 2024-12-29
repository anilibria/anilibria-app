package taiwa.internal.models

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import envoy.DiffItem
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor

internal data class TaiwaContentState(
    val anchor: TaiwaAnchor,
    val header: TaiwaHeaderState?,
    val message: TaiwaMessageState?,
    val items: TaiwaItemsState?,
    val buttons: TaiwaButtonsState?,
    val closeListener: ClickListener?,
)

internal data class TaiwaRootContentState(
    val content: TaiwaContentState,
    val nestedContents: Map<TaiwaAnchor.Id, TaiwaContentState>,
)

internal data class TaiwaHeaderState(
    val title: String?,
    val subtitle: String?,
    val backAction: TaiwaAction?,
    val canClose: Boolean,
)

internal data class TaiwaMessageState(
    val text: String?,
)

internal data class TaiwaItemsState(
    val items: List<DiffItem>,
)

internal data class TaiwaItemState(
    val base: TaiwaBaseItemState,
    val type: TaiwaItemTypeState,
) : DiffItem(base.id)

internal sealed interface TaiwaItemTypeState {

    data class Basic(
        val value: String?,
        val forward: Boolean,
    ) : TaiwaItemTypeState

    data class Switch(
        val selected: Boolean,
    ) : TaiwaItemTypeState

    data class Radio(
        val selected: Boolean,
    ) : TaiwaItemTypeState

    data class Checkbox(
        val selected: Boolean,
    ) : TaiwaItemTypeState
}

internal data class TaiwaBaseItemState(
    val id: ItemId,
    val title: String?,
    val subtitle: String?,
    @DrawableRes val iconRes: Int?,
    val isEmptyIcon: Boolean,
    @AttrRes val tintAttrRes: Int?,
    val action: TaiwaAction?,
    val clickListener: ClickListener?,
)

internal data class TaiwaButtonsState(
    val buttons: List<TaiwaButtonState>,
)

internal data class TaiwaButtonState(
    val id: ButtonId,
    val text: String?,
    val action: TaiwaAction?,
    val clickListener: ClickListener?,
)