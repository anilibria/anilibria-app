package taiwa.internal.dsl

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import envoy.DiffItem
import taiwa.TaiwaAction
import taiwa.TaiwaAnchor
import taiwa.dsl.TaiwaBaseItemScope
import taiwa.dsl.TaiwaBasicItemScope
import taiwa.dsl.TaiwaButtonScope
import taiwa.dsl.TaiwaButtonsScope
import taiwa.dsl.TaiwaCheckboxItemScope
import taiwa.dsl.TaiwaContentScope
import taiwa.dsl.TaiwaMessageScope
import taiwa.dsl.TaiwaNestingScope
import taiwa.dsl.TaiwaRadioItemScope
import taiwa.dsl.TaiwaScope
import taiwa.dsl.TaiwaSwitchItemScope
import taiwa.dsl.TaiwaToolbarScope
import taiwa.internal.models.ClickListener
import taiwa.internal.models.TaiwaButtonState
import taiwa.internal.models.TaiwaButtonsState
import taiwa.internal.models.TaiwaContentState
import taiwa.internal.models.TaiwaItemState
import taiwa.internal.models.TaiwaMessageState
import taiwa.internal.models.TaiwaNestingState
import taiwa.internal.models.TaiwaState
import taiwa.internal.models.TaiwaToolbarState

internal interface ScopeBuilder<State> {
    fun build(): State
}

internal class TaiwaScopeImpl(
    private val anchor: TaiwaAnchor,
) : TaiwaScope, ScopeBuilder<TaiwaState> {

    private var _backAction: TaiwaAction? = null
    private var _closeListener: ClickListener? = null
    private var _header: TaiwaContentState? = null
    private var _body: TaiwaContentState? = null
    private var _footer: TaiwaContentState? = null

    override fun backAction(action: TaiwaAction) {
        _backAction = action
    }

    override fun onClose(listener: ClickListener) {
        _closeListener = listener
    }

    override fun header(block: TaiwaContentScope.() -> Unit) {
        _header = buildContent("header", block)
    }

    override fun body(block: TaiwaContentScope.() -> Unit) {
        _body = buildContent("body", block)
    }

    override fun footer(block: TaiwaContentScope.() -> Unit) {
        _footer = buildContent("footer", block)
    }

    private fun buildContent(
        prefix: String,
        block: TaiwaContentScope.() -> Unit
    ): TaiwaContentState {
        val scope = TaiwaContentScopeImpl(prefix)
        block.invoke(scope)
        return scope.build()
    }


    override fun build(): TaiwaState {
        return TaiwaState(
            anchor = anchor,
            header = _header,
            body = _body,
            footer = _footer,
            backAction = _backAction,
            closeListener = _closeListener
        )
    }
}

internal class TaiwaNestingScopeImpl(
    private val contentScope: TaiwaScopeImpl,
) : TaiwaScope by contentScope, TaiwaNestingScope, ScopeBuilder<TaiwaNestingState> {

    private var _nestedContents = mutableMapOf<TaiwaAnchor.Id, TaiwaState>()

    override fun nested(anchor: TaiwaAnchor.Id, block: TaiwaScope.() -> Unit) {
        val scope = TaiwaScopeImpl(anchor)
        block.invoke(scope)
        _nestedContents[anchor] = scope.build()
    }

    override fun build(): TaiwaNestingState {
        return TaiwaNestingState(
            content = contentScope.build(),
            nested = _nestedContents
        )
    }
}

internal class TaiwaContentScopeImpl(
    private val prefix: String
) : TaiwaContentScope, ScopeBuilder<TaiwaContentState> {

    private val _items = mutableListOf<DiffItem>()

    override fun envoy(item: DiffItem) {
        _items.add(item)
    }

    override fun toolbar(block: TaiwaToolbarScope.() -> Unit) {
        val scope = TaiwaToolbarScopeImpl(getNextId(null))
        block.invoke(scope)
        _items.add(scope.build())
    }

    override fun message(block: TaiwaMessageScope.() -> Unit) {
        val scope = TaiwaMessageScopeImpl(getNextId(null))
        block.invoke(scope)
        _items.add(scope.build())
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

    override fun buttons(block: TaiwaButtonsScope.() -> Unit) {
        val scope = TaiwaButtonsScopeImpl(getNextId(null))
        block.invoke(scope)
        _items.add(scope.build())
    }

    override fun build(): TaiwaContentState {
        return TaiwaContentState(
            items = _items
        )
    }

    private fun getNextId(id: Any?): Any {
        return id ?: "taiwa_${prefix}_${_items.size}"
    }

    private fun createBaseItemScope(id: Any?): TaiwaBaseItemScopeImpl {
        return TaiwaBaseItemScopeImpl(
            _id = getNextId(id),
        )
    }
}

internal class TaiwaToolbarScopeImpl(
    private val _id: Any
) : TaiwaToolbarScope, ScopeBuilder<TaiwaToolbarState> {
    private var _title: String? = null
    private var _subtitle: String? = null
    private var _withBack: Boolean = false
    private var _withClose: Boolean = false

    override fun title(value: String) {
        _title = value
    }

    override fun subtitle(value: String) {
        _subtitle = value
    }

    override fun withBack() {
        _withBack = true
    }

    override fun withClose() {
        _withClose = true
    }

    override fun build(): TaiwaToolbarState {
        return TaiwaToolbarState(
            id = _id,
            title = _title,
            subtitle = _subtitle,
            withBack = _withBack,
            withClose = _withClose
        )
    }
}

internal class TaiwaMessageScopeImpl(
    private val _id: Any
) : TaiwaMessageScope, ScopeBuilder<TaiwaMessageState> {
    private var _text: String? = null

    override fun text(value: String) {
        _text = value
    }

    override fun build(): TaiwaMessageState {
        return TaiwaMessageState(
            id = _id,
            text = _text
        )
    }
}


internal class TaiwaBaseItemScopeImpl(
    private val _id: Any,
) : TaiwaBaseItemScope, ScopeBuilder<TaiwaItemState.Base> {

    private var _iconRes: Int? = null
    private var _isEmptyIcon: Boolean = false
    private var _tintAttrRes: Int? = null
    private var _title: String? = null
    private var _subtitle: String? = null
    private var _action: TaiwaAction? = null
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

    override fun action(action: TaiwaAction) {
        _action = action
    }

    override fun onClick(listener: ClickListener) {
        _clickListener = listener
    }

    override fun build(): TaiwaItemState.Base {
        return TaiwaItemState.Base(
            id = _id,
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
            type = TaiwaItemState.Type.Basic(
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
            type = TaiwaItemState.Type.Switch(_selected)
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
            type = TaiwaItemState.Type.Radio(_selected)
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
            type = TaiwaItemState.Type.Checkbox(_selected)
        )
    }
}

internal class TaiwaButtonsScopeImpl(
    private val _id: Any
) : TaiwaButtonsScope, ScopeBuilder<TaiwaButtonsState> {

    private var _action: TaiwaAction? = null
    private val _buttons = mutableListOf<TaiwaButtonState>()

    override fun action(action: TaiwaAction) {
        _action = action
    }

    override fun button(id: Any?, block: TaiwaButtonScope.() -> Unit) {
        val scope = TaiwaButtonScopeImpl(createButtonId(id), _action)
        block.invoke(scope)
        _buttons.add(scope.build())
    }

    override fun build(): TaiwaButtonsState {
        return TaiwaButtonsState(
            id = _id,
            buttons = _buttons
        )
    }

    private fun createButtonId(id: Any?): Any {
        return id ?: "taiwa_button_${_buttons.lastIndex}"
    }
}

internal class TaiwaButtonScopeImpl(
    private val _id: Any,
    private var _action: TaiwaAction?,
) : TaiwaButtonScope, ScopeBuilder<TaiwaButtonState> {

    private var _text: String? = null

    private var _clickListener: ClickListener? = null

    override fun text(value: String) {
        _text = value
    }

    override fun action(action: TaiwaAction) {
        _action = action
    }

    override fun onClick(listener: ClickListener) {
        _clickListener = listener
    }

    override fun build(): TaiwaButtonState {
        return TaiwaButtonState(
            id = _id,
            text = _text,
            action = _action,
            clickListener = _clickListener
        )
    }
}