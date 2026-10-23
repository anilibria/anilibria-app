package tv.aniliberty.next

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

internal fun playbackTime(ms: Long): String {
    val total = ms.coerceAtLeast(0) / 1000
    return if (total >= 3600) "%d:%02d:%02d".format(total / 3600, total / 60 % 60, total % 60)
    else "%d:%02d".format(total / 60, total % 60)
}

/** All actions fit in one TV row; secondary settings live in the side panel. */
class PlayerChrome(context: Context) : LinearLayout(context) {
    val title =
        TextView(context).apply {
            label("Загрузка…", 22f, WHITE, true)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
    val episode =
        TextView(context).apply {
            label("", 14f, MUTED)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
    val elapsed = TextView(context).apply { label("0:00", 14f, WHITE, true) }
    val remaining =
        TextView(context).apply {
            label("", 13f, MUTED)
            gravity = Gravity.END
        }
    val seek =
        SeekBar(context).apply {
            max = 1000
            progressTintList = android.content.res.ColorStateList.valueOf(RED)
            thumbTintList = android.content.res.ColorStateList.valueOf(WHITE)
            contentDescription = "Перемотка"
            tag = "player-seek"
            focusBackground(radiusDp = 8)
        }
    val skipActions =
        LinearLayout(context).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            visibility = View.GONE
            tag = "player-skip-actions"
        }
    val buttons = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL }

    init {
        orientation = VERTICAL
        setPadding(dp(32), dp(28), dp(32), dp(16))
        background =
            GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x00101010, 0xE8101010.toInt(), 0xFC101010.toInt()),
            )
        addView(
            LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(title, LayoutParams(0, -2, 2f))
                episode.gravity = Gravity.END
                addView(episode, LayoutParams(0, -2, 1f).apply { marginStart = dp(16) })
            },
            LayoutParams(-1, -2).apply { bottomMargin = dp(12) },
        )
        addView(
            LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(elapsed, LayoutParams(0, -2, 1f))
                addView(remaining, LayoutParams(0, -2, 1f))
            }
        )
        addView(
            skipActions,
            LayoutParams(-1, dp(48)).apply {
                topMargin = dp(4)
                bottomMargin = dp(4)
            },
        )
        addView(seek, LayoutParams(-1, dp(36)))
        addView(
            buttons,
            LayoutParams(-1, dp(56)).apply {
                topMargin = dp(6)
                bottomMargin = dp(10)
            },
        )
    }

    fun icon(label: String, icon: Int, primary: Boolean = false, click: () -> Unit): TextView =
        action(buttons, "", primary, icon, click).apply {
            contentDescription = label
            tag = "player:$label"
            setPadding(dp(16), 0, dp(16), 0)
            compoundDrawablePadding = 0
            buttons.addView(
                this,
                LayoutParams(dp(if (primary) 60 else 52), dp(52)).apply { marginEnd = dp(8) },
            )
        }

    fun button(label: String, icon: Int, click: () -> Unit): TextView =
        action(buttons, label, icon = icon, click = click).apply {
            textSize = 14f
            maxLines = 1
            setPadding(dp(12), 0, dp(12), 0)
            buttons.addView(this, LayoutParams(-2, dp(48)).apply { marginEnd = dp(8) })
        }

    fun update(position: Long, duration: Long) {
        elapsed.text = playbackTime(position)
        remaining.text =
            if (duration > 0)
                "−${playbackTime((duration-position).coerceAtLeast(0))}   /   ${playbackTime(duration)}"
            else ""
        seek.isEnabled = duration > 0
        seek.progress =
            if (duration > 0) (position.coerceIn(0, duration) * 1000 / duration).toInt() else 0
    }
}
