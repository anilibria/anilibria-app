package taiwa.dsl

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import envoy.DiffItem
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor
import taiwa.internal.models.ClickListener


@DslMarker
annotation class TaiwaScopeMarker

@DslMarker
annotation class ContentScopeMarker

@DslMarker
annotation class ToolbarScopeMarker

@DslMarker
annotation class MessageScopeMarker

@DslMarker
annotation class ButtonsScopeMarker

@DslMarker
annotation class ButtonScopeMarker

@DslMarker
annotation class ChipsScopeMarker

@DslMarker
annotation class ChipScopeMarker

@DslMarker
annotation class ItemsScopeMarker

@DslMarker
annotation class ItemScopeMarker

@ItemScopeMarker
@ToolbarScopeMarker
@MessageScopeMarker
@ButtonScopeMarker
@ChipScopeMarker
@TaiwaScopeMarker
@ContentScopeMarker
interface TaiwaContentScope {
    fun envoy(item: DiffItem)
    fun toolbar(block: TaiwaToolbarScope.() -> Unit)
    fun message(block: TaiwaMessageScope.() -> Unit)
    fun item(id: Any? = null, block: TaiwaBasicItemScope.() -> Unit)
    fun switchItem(id: Any? = null, block: TaiwaSwitchItemScope.() -> Unit)
    fun radioItem(id: Any? = null, block: TaiwaRadioItemScope.() -> Unit)
    fun checkboxItem(id: Any? = null, block: TaiwaCheckboxItemScope.() -> Unit)
    fun buttons(block: TaiwaButtonsScope.() -> Unit)
    fun chips(block: TaiwaChipsScope.() -> Unit)
}

@TaiwaScopeMarker
@ToolbarScopeMarker
@MessageScopeMarker
@ItemsScopeMarker
interface TaiwaScope {
    fun backAction(action: TaiwaAction)
    fun onClose(listener: ClickListener)
    fun header(block: TaiwaContentScope.() -> Unit)
    fun body(block: TaiwaContentScope.() -> Unit)
    fun footer(block: TaiwaContentScope.() -> Unit)
}

interface TaiwaNestingScope : TaiwaScope {
    fun nested(anchor: TaiwaAnchor.Id, block: TaiwaScope.() -> Unit)
}

@TaiwaScopeMarker
@ToolbarScopeMarker
interface TaiwaToolbarScope {
    fun title(value: String)
    fun subtitle(value: String)
    fun withBack()
    fun withClose()
}

@TaiwaScopeMarker
@MessageScopeMarker
interface TaiwaMessageScope {
    fun text(value: String)
}

@TaiwaScopeMarker
@ButtonsScopeMarker
interface TaiwaButtonsScope {
    fun action(action: TaiwaAction)
    fun button(id: Any? = null, block: TaiwaButtonScope.() -> Unit)
}

@TaiwaScopeMarker
@ChipsScopeMarker
interface TaiwaChipsScope {
    fun action(action: TaiwaAction)
    fun chip(id: Any? = null, block: TaiwaChipScope.() -> Unit)
}

@ItemsScopeMarker
@ItemScopeMarker
interface TaiwaBaseItemScope {
    fun icon(@DrawableRes iconRes: Int)
    fun emptyIcon()
    fun tint(@AttrRes attrRes: Int)
    fun title(value: String)
    fun subtitle(value: String)
    fun action(action: TaiwaAction)
    fun onClick(listener: ClickListener)
}

interface TaiwaBasicItemScope : TaiwaBaseItemScope {
    fun value(value: String)
    fun forward()
}

interface TaiwaSelectableScope {
    fun select(value: Boolean = true)
}

interface TaiwaSwitchItemScope : TaiwaBaseItemScope, TaiwaSelectableScope

interface TaiwaRadioItemScope : TaiwaBaseItemScope, TaiwaSelectableScope

interface TaiwaCheckboxItemScope : TaiwaBaseItemScope, TaiwaSelectableScope

@ButtonsScopeMarker
@ButtonScopeMarker
interface TaiwaButtonScope {
    fun text(value: String)
    fun action(action: TaiwaAction)
    fun onClick(listener: ClickListener)
}

@ChipsScopeMarker
@ChipScopeMarker
interface TaiwaChipScope : TaiwaSelectableScope {
    fun text(value: String)
    fun action(action: TaiwaAction)
    fun onClick(listener: ClickListener)
}