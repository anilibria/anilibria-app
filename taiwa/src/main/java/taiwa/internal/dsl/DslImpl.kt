package taiwa.internal.dsl

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor
import taiwa.dsl.TaiwaBaseItemScope
import taiwa.dsl.TaiwaBasicItemScope
import taiwa.dsl.TaiwaButtonScope
import taiwa.dsl.TaiwaButtonsScope
import taiwa.dsl.TaiwaCheckboxItemScope
import taiwa.dsl.TaiwaContentScope
import taiwa.dsl.TaiwaHeaderScope
import taiwa.dsl.TaiwaItemsScope
import taiwa.dsl.TaiwaMessageScope
import taiwa.dsl.TaiwaRadioItemScope
import taiwa.dsl.TaiwaRootContentScope
import taiwa.dsl.TaiwaSwitchItemScope
import taiwa.internal.models.ButtonId
import taiwa.internal.models.ClickListener
import taiwa.internal.models.ItemId
import taiwa.internal.models.TaiwaBaseItemState
import taiwa.internal.models.TaiwaButtonState
import taiwa.internal.models.TaiwaButtonsState
import taiwa.internal.models.TaiwaContentState
import taiwa.internal.models.TaiwaHeaderState
import taiwa.internal.models.TaiwaItemState
import taiwa.internal.models.TaiwaItemTypeState
import taiwa.internal.models.TaiwaItemsState
import taiwa.internal.models.TaiwaMessageState
import taiwa.internal.models.TaiwaRootContentState

internal interface ScopeBuilder<State> {
    fun build(): State
}

internal class TaiwaContentScopeImpl(
    private val anchor: TaiwaAnchor,
) : TaiwaContentScope, ScopeBuilder<TaiwaContentState> {

    private var _closeListener: ClickListener? = null
    private var _header: TaiwaHeaderState? = null
    private var _message: TaiwaMessageState? = null
    private var _items: TaiwaItemsState? = null
    private var _buttons: TaiwaButtonsState? = null

    override fun onClose(listener: ClickListener) {
        _closeListener = listener
    }

    override fun header(block: TaiwaHeaderScope.() -> Unit) {
        val scope = TaiwaHeaderScopeImpl()
        block.invoke(scope)
        _header = scope.build()
    }

    override fun message(block: TaiwaMessageScope.() -> Unit) {
        val scope = TaiwaMessageScopeImpl()
        block.invoke(scope)
        _message = scope.build()
    }

    override fun items(block: TaiwaItemsScope.() -> Unit) {
        val scope = TaiwaItemsScopeImpl()
        block.invoke(scope)
        _items = scope.build()
    }

    override fun buttons(block: TaiwaButtonsScope.() -> Unit) {
        val scope = TaiwaButtonsScopeImpl()
        block.invoke(scope)
        _buttons = scope.build()
    }

    override fun build(): TaiwaContentState {
        return TaiwaContentState(
            anchor = anchor,
            header = _header,
            message = _message,
            items = _items,
            buttons = _buttons,
            closeListener = _closeListener
        )
    }
}

internal class TaiwaRootContentScopeImpl(
    private val contentScope: TaiwaContentScopeImpl,
) : TaiwaContentScope by contentScope, TaiwaRootContentScope, ScopeBuilder<TaiwaRootContentState> {

    private var _nestedContents = mutableMapOf<TaiwaAnchor.Id, TaiwaContentState>()

    override fun nestedContent(anchor: TaiwaAnchor.Id, block: TaiwaContentScope.() -> Unit) {
        val scope = TaiwaContentScopeImpl(anchor)
        block.invoke(scope)
        _nestedContents[anchor] = scope.build()
    }

    override fun build(): TaiwaRootContentState {
        return TaiwaRootContentState(
            content = contentScope.build(),
            nestedContents = _nestedContents
        )
    }
}

internal class TaiwaHeaderScopeImpl : TaiwaHeaderScope, ScopeBuilder<TaiwaHeaderState> {
    private var _title: String? = null
    private var _subtitle: String? = null
    private var _backAction: TaiwaAction? = null
    private var _canClose: Boolean = false

    override fun title(value: String) {
        _title = value
    }

    override fun subtitle(value: String) {
        _subtitle = value
    }

    override fun backAction(action: TaiwaAction?) {
        _backAction = action
    }

    override fun canClose() {
        _canClose = true
    }

    override fun build(): TaiwaHeaderState {
        return TaiwaHeaderState(
            title = _title,
            subtitle = _subtitle,
            backAction = _backAction,
            canClose = _canClose
        )
    }
}

internal class TaiwaMessageScopeImpl : TaiwaMessageScope, ScopeBuilder<TaiwaMessageState> {
    private var _text: String? = null

    override fun text(value: String) {
        _text = value
    }

    override fun build(): TaiwaMessageState {
        return TaiwaMessageState(
            text = _text
        )
    }
}


internal class TaiwaItemsScopeImpl : TaiwaItemsScope, ScopeBuilder<TaiwaItemsState> {

    private var _action: TaiwaAction? = null
    private val _items = mutableListOf<TaiwaItemState>()

    override fun action(action: TaiwaAction?) {
        _action = action
    }

    override fun item(id: Any?, block: TaiwaBasicItemScope.() -> Unit) {
        val scope = TaiwaBasicItemScopeImpl(createBaseItemScope(id))
        block.invoke(scope)
        _items.add(scope.build())
    }

    override fun switchItem(id: Any?, block: TaiwaSwitchItemScope.() -> Unit) {
        val scope = TaiwaSwitchItemScopeImpl(createBaseItemScope(id))
        block.invoke(scope)
        _items.add(scope.build())
    }

    override fun radioItem(id: Any?, block: TaiwaRadioItemScope.() -> Unit) {
        val scope = TaiwaRadioItemScopeImpl(createBaseItemScope(id))
        block.invoke(scope)
        _items.add(scope.build())
    }

    override fun checkboxItem(id: Any?, block: TaiwaCheckboxItemScope.() -> Unit) {
        val scope = TaiwaCheckboxItemScopeImpl(createBaseItemScope(id))
        block.invoke(scope)
        _items.add(scope.build())
    }

    override fun build(): TaiwaItemsState {
        return TaiwaItemsState(
            items = _items,
        )
    }

    private fun createBaseItemScope(id: Any?): TaiwaBaseItemScopeImpl {
        val idValue = id ?: "taiwa_item_${_items.lastIndex}"
        return TaiwaBaseItemScopeImpl(
            itemId = ItemId(idValue),
            _action = _action
        )
    }

}

internal class TaiwaBaseItemScopeImpl(
    private val itemId: ItemId,
    private var _action: TaiwaAction?,
) : TaiwaBaseItemScope, ScopeBuilder<TaiwaBaseItemState> {

    private var _iconRes: Int? = null
    private var _isEmptyIcon: Boolean = false
    private var _tintAttrRes: Int? = null
    private var _title: String? = null
    private var _subtitle: String? = null
    private var _clickListener: ClickListener? = null

    override fun icon(@DrawableRes iconRes: Int) {
        _iconRes = iconRes
    }

    override fun emptyIcon() {
        _isEmptyIcon = true
    }

    override fun tint(@AttrRes attrRes: Int) {
        _tintAttrRes = attrRes
    }

    override fun title(value: String) {
        _title = value
    }

    override fun subtitle(value: String) {
        _subtitle = value
    }

    override fun action(action: TaiwaAction?) {
        _action = action
    }

    override fun onClick(listener: ClickListener) {
        _clickListener = listener
    }

    override fun build(): TaiwaBaseItemState {
        return TaiwaBaseItemState(
            id = itemId,
            title = _title,
            subtitle = _subtitle,
            iconRes = _iconRes,
            isEmptyIcon = _isEmptyIcon,
            tintAttrRes = _tintAttrRes,
            action = _action,
            clickListener = _clickListener
        )
    }
}

internal class TaiwaBasicItemScopeImpl(
    private val base: TaiwaBaseItemScopeImpl,
) : TaiwaBasicItemScope, TaiwaBaseItemScope by base, ScopeBuilder<TaiwaItemState> {

    private var _value: String? = null
    private var _forward: Boolean = false

    override fun value(value: String) {
        _value = value
    }

    override fun forward() {
        _forward = true
    }

    override fun build(): TaiwaItemState {
        return TaiwaItemState(
            base = base.build(),
            type = TaiwaItemTypeState.Basic(
                value = _value,
                forward = _forward
            )
        )
    }
}

internal class TaiwaSwitchItemScopeImpl(
    private val base: TaiwaBaseItemScopeImpl,
) : TaiwaSwitchItemScope, TaiwaBaseItemScope by base, ScopeBuilder<TaiwaItemState> {

    private var _selected = false

    override fun select(value: Boolean) {
        _selected = value
    }

    override fun build(): TaiwaItemState {
        return TaiwaItemState(
            base = base.build(),
            type = TaiwaItemTypeState.Switch(_selected)
        )
    }
}

internal class TaiwaRadioItemScopeImpl(
    private val base: TaiwaBaseItemScopeImpl,
) : TaiwaRadioItemScope, TaiwaBaseItemScope by base, ScopeBuilder<TaiwaItemState> {

    private var _selected = false

    override fun select(value: Boolean) {
        _selected = value
    }

    override fun build(): TaiwaItemState {
        return TaiwaItemState(
            base = base.build(),
            type = TaiwaItemTypeState.Radio(_selected)
        )
    }
}

internal class TaiwaCheckboxItemScopeImpl(
    private val base: TaiwaBaseItemScopeImpl,
) : TaiwaCheckboxItemScope, TaiwaBaseItemScope by base, ScopeBuilder<TaiwaItemState> {

    private var _selected = false

    override fun select(value: Boolean) {
        _selected = value
    }

    override fun build(): TaiwaItemState {
        return TaiwaItemState(
            base = base.build(),
            type = TaiwaItemTypeState.Checkbox(_selected)
        )
    }
}

internal class TaiwaButtonsScopeImpl : TaiwaButtonsScope, ScopeBuilder<TaiwaButtonsState> {

    private var _action: TaiwaAction? = null
    private val _buttons = mutableListOf<TaiwaButtonState>()

    override fun action(action: TaiwaAction?) {
        _action = action
    }

    override fun button(id: Any?, block: TaiwaButtonScope.() -> Unit) {
        val scope = TaiwaButtonScopeImpl(createButtonId(id), _action)
        block.invoke(scope)
        _buttons.add(scope.build())
    }

    override fun build(): TaiwaButtonsState {
        return TaiwaButtonsState(buttons = _buttons)
    }

    private fun createButtonId(id: Any?): ButtonId {
        return ButtonId(id ?: "taiwa_button_${_buttons.lastIndex}")
    }
}

internal class TaiwaButtonScopeImpl(
    private val buttonId: ButtonId,
    private var _action: TaiwaAction?,
) : TaiwaButtonScope, ScopeBuilder<TaiwaButtonState> {

    private var _text: String? = null

    private var _clickListener: ClickListener? = null

    override fun text(value: String) {
        _text = value
    }

    override fun action(action: TaiwaAction?) {
        _action = action
    }

    override fun onClick(listener: ClickListener) {
        _clickListener = listener
    }

    override fun build(): TaiwaButtonState {
        return TaiwaButtonState(
            id = buttonId,
            text = _text,
            action = _action,
            clickListener = _clickListener
        )
    }
}