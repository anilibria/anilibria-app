package tv.aniliberty.next

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.EditText

/** First presses are never delayed; only excessive hardware repeats are coalesced. */
internal class RemoteRepeat(private val interval: Long = 90) {
    private var key = -1
    private var last = Long.MIN_VALUE

    fun discard(event: KeyEvent): Boolean {
        if (
            event.keyCode !in
                listOf(
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_DPAD_RIGHT,
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_DOWN,
                )
        )
            return false
        if (event.action == KeyEvent.ACTION_UP) {
            key = -1
            return false
        }
        if (event.action != KeyEvent.ACTION_DOWN) return false
        if (event.repeatCount == 0 || key != event.keyCode) {
            key = event.keyCode
            last = event.eventTime
            return false
        }
        if (event.eventTime - last < interval) return true
        last = event.eventTime
        return false
    }
}

/** Own the whole OK gesture, including long press, rather than mixing key-up and native clicks. */
internal class RemoteClick {
    private val handler = Handler(Looper.getMainLooper())
    private var target: View? = null
    private var key = -1
    private var longHandled = false
    private val hold = Runnable {
        target
            ?.takeIf {
                it.isAttachedToWindow && it.hasFocus() && it.isEnabled && it.isLongClickable
            }
            ?.let {
                longHandled = it.performLongClick()
                it.isPressed = false
            }
    }

    fun cancel() {
        handler.removeCallbacks(hold)
        target?.isPressed = false
        target = null
        key = -1
        longHandled = false
    }

    fun handle(event: KeyEvent, focused: View?): Boolean {
        if (
            event.keyCode !in
                listOf(
                    KeyEvent.KEYCODE_DPAD_CENTER,
                    KeyEvent.KEYCODE_ENTER,
                    KeyEvent.KEYCODE_NUMPAD_ENTER,
                )
        ) {
            if (event.action == KeyEvent.ACTION_DOWN) cancel()
            return false
        }
        if (focused is EditText) return false
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (event.repeatCount == 0) {
                cancel()
                if (
                    focused?.isEnabled != true || (!focused.isClickable && !focused.isLongClickable)
                )
                    return false
                target = focused
                key = event.keyCode
                focused.isPressed = true
                if (focused.isLongClickable)
                    handler.postDelayed(hold, ViewConfiguration.getLongPressTimeout().toLong())
            }
            return target != null
        }
        if (event.action == KeyEvent.ACTION_UP && key == event.keyCode) {
            val view = target
            val click =
                !longHandled &&
                    !event.isCanceled &&
                    view === focused &&
                    view?.isEnabled == true &&
                    view.isAttachedToWindow
            cancel()
            if (click) view?.performClick()
            return true
        }
        return false
    }
}

internal fun remoteSeekTarget(position: Long, duration: Long, right: Boolean, heldMs: Long): Long? {
    if (duration <= 0) return null
    val step =
        when {
            heldMs >= 2500 -> 30000L
            heldMs >= 1000 -> 20000L
            else -> 10000L
        }
    return (position + (if (right) step else -step)).coerceIn(0, duration)
}
