package tv.aniliberty.next

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** A single, remote-friendly surface shared by catalogue tools. */
open class TvSidePanel(context: Context, title: String) : Dialog(context) {
    private val remoteClick = RemoteClick()
    private val remoteRepeat = RemoteRepeat()
    val heading =
        TextView(context).apply {
            label(title, 24f, WHITE, true)
            includeFontPadding = false
            maxLines = 3
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(4), 0, dp(20))
        }
    val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    val footer = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val scroll =
        ScrollView(context).apply {
            isFillViewport = false
            isFocusable = true
            addView(body)
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    private val shell =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(0xFF212121.toInt())
            addView(heading, LinearLayout.LayoutParams(-1, -2))
            addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
            addView(footer, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
        }
    private var returnFocus: View? = null

    protected fun dp(n: Int) = (context.resources.displayMetrics.density * n).toInt()

    init {
        setContentView(shell)
    }

    fun message(text: String) {
        body.removeAllViews()
        body.addView(
            TextView(context).apply {
                label(text, 16f, MUTED)
                setPadding(0, dp(12), 0, dp(12))
            }
        )
    }

    fun addAction(text: String, primary: Boolean = false, callback: () -> Unit): TextView =
        action(footer, text, primary = primary, click = callback).also {
            footer.addView(it, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(8) })
        }

    fun addChoice(
        text: String,
        selected: Boolean = false,
        icon: Int = 0,
        iconTint: Int? = null,
        callback: () -> Unit,
    ): TextView =
        action(body, if (selected) "✓  $text" else text, icon = icon, click = callback).apply {
            iconTint?.let { tint ->
                compoundDrawablesRelative[0]?.mutate()?.setTint(tint)
            }
            textSize = 16f
            maxLines = 3
            setPadding(dp(12), dp(12), dp(12), dp(12))
            body.addView(this, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (remoteRepeat.discard(event)) return true
        if (remoteClick.handle(event, currentFocus)) return true
        val vertical =
            event.keyCode in
                listOf(
                    android.view.KeyEvent.KEYCODE_DPAD_UP,
                    android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                )
        if (
            vertical &&
                body.getFocusables(View.FOCUS_FORWARD).isEmpty() &&
                (scroll.canScrollVertically(-1) ||
                    scroll.canScrollVertically(1) ||
                    scroll.hasFocus()) &&
                (scroll.hasFocus() ||
                    (currentFocus === footer.getFocusables(View.FOCUS_FORWARD).firstOrNull() &&
                        (event.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP ||
                            scroll.scrollY == 0)))
        ) {
            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                val direction =
                    if (event.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN) 1 else -1
                if (scroll.canScrollVertically(direction)) {
                    scroll.requestFocus()
                    scroll.scrollBy(0, direction * dp(96))
                } else if (direction > 0)
                    footer.getFocusables(View.FOCUS_FORWARD).firstOrNull()?.requestFocus()
            }
            return true
        }
        if (
            event.keyCode in
                listOf(
                    android.view.KeyEvent.KEYCODE_DPAD_UP,
                    android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
                )
        ) {
            val focused = currentFocus
            if (focused is TextView && focused !is android.widget.EditText && focused.isClickable) {
                if (
                    event.action == android.view.KeyEvent.ACTION_DOWN &&
                        event.keyCode in
                            listOf(
                                android.view.KeyEvent.KEYCODE_DPAD_UP,
                                android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                            )
                ) {
                    val choices =
                        body.getFocusables(View.FOCUS_FORWARD) +
                            footer.getFocusables(View.FOCUS_FORWARD)
                    val index = choices.indexOf(focused)
                    choices
                        .getOrNull(
                            index +
                                if (event.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN) 1
                                else -1
                        )
                        ?.requestFocus()
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dismiss() {
        remoteClick.cancel()
        super.dismiss()
        returnFocus?.let { caller ->
            caller.post { if (caller.isAttachedToWindow && caller.isShown) caller.requestFocus() }
        }
    }

    override fun show() {
        if (!isShowing) returnFocus = (context as? android.app.Activity)?.currentFocus
        super.show()
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout((context.resources.displayMetrics.widthPixels * .42f).toInt(), -1)
            setGravity(Gravity.END)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(.55f)
        }
        body.getFocusables(View.FOCUS_FORWARD).firstOrNull()?.requestFocus()
            ?: footer.getFocusables(View.FOCUS_FORWARD).firstOrNull()?.requestFocus()
    }
}
