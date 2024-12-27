package taiwa.dsl

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor
import taiwa.internal.models.ClickListener


@DslMarker
annotation class ContentScopeMarker

@DslMarker
annotation class HeaderScopeMarker

@DslMarker
annotation class MessageScopeMarker

@DslMarker
annotation class ButtonsScopeMarker

@DslMarker
annotation class ButtonScopeMarker

@DslMarker
annotation class ItemsScopeMarker

@DslMarker
annotation class ItemScopeMarker

@ContentScopeMarker
@HeaderScopeMarker
@MessageScopeMarker
@ItemsScopeMarker
@ButtonScopeMarker
interface TaiwaContentScope {
    fun onClose(listener: ClickListener)
    fun header(block: TaiwaHeaderScope.() -> Unit)
    fun message(block: TaiwaMessageScope.() -> Unit)
    fun items(block: TaiwaItemsScope.() -> Unit)
    fun buttons(block: TaiwaButtonsScope.() -> Unit)
}

interface TaiwaRootContentScope : TaiwaContentScope {
    fun nestedContent(anchor: TaiwaAnchor.Id, block: TaiwaContentScope.() -> Unit)
}

@ContentScopeMarker
@HeaderScopeMarker
interface TaiwaHeaderScope {
    fun title(value: String)
    fun subtitle(value: String)
    fun backAction(action: TaiwaAction?)
    fun canClose()
}

@ContentScopeMarker
@MessageScopeMarker
interface TaiwaMessageScope {
    fun text(value: String)
}

@ContentScopeMarker
@ItemsScopeMarker
interface TaiwaItemsScope {
    fun action(action: TaiwaAction?)
    fun item(id: Any? = null, block: TaiwaBasicItemScope.() -> Unit)
    fun switchItem(id: Any? = null, block: TaiwaSwitchItemScope.() -> Unit)
    fun radioItem(id: Any? = null, block: TaiwaRadioItemScope.() -> Unit)
    fun checkboxItem(id: Any? = null, block: TaiwaCheckboxItemScope.() -> Unit)
}

@ContentScopeMarker
@ButtonsScopeMarker
interface TaiwaButtonsScope {
    fun action(action: TaiwaAction?)
    fun button(id: Any? = null, block: TaiwaButtonScope.() -> Unit)
}

@ItemsScopeMarker
@ItemScopeMarker
interface TaiwaBaseItemScope {
    fun icon(@DrawableRes iconRes: Int)
    fun emptyIcon()
    fun tint(@AttrRes attrRes: Int)
    fun title(value: String)
    fun subtitle(value: String)
    fun action(action: TaiwaAction?)
    fun onClick(listener: ClickListener)
}

interface TaiwaBasicItemScope : TaiwaBaseItemScope {
    fun value(value: String)
    fun forward()
}

interface TaiwaSelectableItemScope {
    fun select(value: Boolean = true)
}

interface TaiwaSwitchItemScope : TaiwaBaseItemScope, TaiwaSelectableItemScope

interface TaiwaRadioItemScope : TaiwaBaseItemScope, TaiwaSelectableItemScope

interface TaiwaCheckboxItemScope : TaiwaBaseItemScope, TaiwaSelectableItemScope


@ButtonsScopeMarker
@ButtonScopeMarker
interface TaiwaButtonScope {
    fun text(value: String)
    fun action(action: TaiwaAction?)
    fun onClick(listener: ClickListener)
}