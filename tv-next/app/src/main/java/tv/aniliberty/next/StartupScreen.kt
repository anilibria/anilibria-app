package tv.aniliberty.next

import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

/** One TV surface for welcome, initial loading and a recoverable startup error. */
internal class StartupScreen(
    context: Context,
    start: () -> Unit,
    retry: () -> Unit,
    qr: () -> Unit = {},
    password: () -> Unit = {},
) : FrameLayout(context) {
    private val title: TextView
    private val description: TextView
    private val progress: ProgressBar
    private val begin: TextView
    private val qrLogin: TextView
    private val passwordLogin: TextView
    private val repeat: TextView
    private var mode: String? = null

    init {
        setBackgroundColor(0xFF121212.toInt())
        val content =
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(32), dp(20), dp(32), dp(20))
            }
        addView(content, LayoutParams(-1, -2, Gravity.CENTER))
        content.addView(
            ImageView(context).apply {
                setImageResource(R.drawable.ic_aniliberty)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            },
            LinearLayout.LayoutParams(dp(64), dp(64)).apply { bottomMargin = dp(8) },
        )
        content.addView(
            TextView(context).apply {
                label("AniLiberty", 32f, WHITE, true)
                gravity = Gravity.CENTER
            }
        )
        title = TextView(context).apply { gravity = Gravity.CENTER }
        content.addView(title, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        description =
            TextView(context).apply {
                gravity = Gravity.CENTER
                maxLines = 3
            }
        content.addView(description, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        progress =
            ProgressBar(context).apply {
                isIndeterminate = true
                indeterminateTintList = ColorStateList.valueOf(RED)
                contentDescription = "Загрузка"
            }
        content.addView(
            progress,
            LinearLayout.LayoutParams(dp(32), dp(32)).apply { topMargin = dp(24) },
        )
        qrLogin = action(content, "Войти по QR-коду", primary = true, click = qr)
        content.addView(
            qrLogin,
            LinearLayout.LayoutParams(dp(320), dp(46)).apply { topMargin = dp(16) },
        )
        passwordLogin = action(content, "Войти по логину и паролю", click = password)
        content.addView(
            passwordLogin,
            LinearLayout.LayoutParams(dp(320), dp(46)).apply { topMargin = dp(8) },
        )
        begin = action(content, "Продолжить как гость", click = start)
        content.addView(
            begin,
            LinearLayout.LayoutParams(dp(320), dp(46)).apply { topMargin = dp(8) },
        )
        repeat =
            action(
                content,
                "Повторить попытку",
                primary = true,
                icon = R.drawable.ic_refresh,
                click = retry,
            )
        content.addView(
            repeat,
            LinearLayout.LayoutParams(dp(320), dp(46)).apply { topMargin = dp(8) },
        )
    }

    fun show(welcome: Boolean, error: Boolean) {
        val nextMode = if (welcome) "welcome" else if (error) "error" else "loading"
        if (mode == nextMode) return
        mode = nextMode
        title.label(
            when {
                welcome -> "Аниме на большом экране"
                error -> "Не удалось подключиться"
                else -> "Загрузка…"
            },
            20f,
            WHITE,
            true,
        )
        description.label(
            when {
                welcome ->
                    "Войдите для синхронизации списков и прогресса\nили начните просмотр без аккаунта."
                error -> "Проверьте подключение к интернету и попробуйте ещё раз."
                else -> ""
            },
            15f,
            MUTED,
        )
        qrLogin.visibility = if (welcome) View.VISIBLE else View.GONE
        passwordLogin.visibility = if (welcome) View.VISIBLE else View.GONE
        description.visibility = if (description.text.isBlank()) View.GONE else View.VISIBLE
        begin.visibility = if (welcome) View.VISIBLE else View.GONE
        repeat.visibility = if (error) View.VISIBLE else View.GONE
        progress.visibility = if (!welcome && !error) View.VISIBLE else View.GONE
        if (welcome || error) (if (welcome) qrLogin else repeat).requestFocus()
    }
}
