package tv.aniliberty.next

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class StartupScreenTest {
    private fun labels(view: View): List<TextView> =
        when (view) {
            is TextView -> listOf(view)
            is ViewGroup -> (0 until view.childCount).flatMap { labels(view.getChildAt(it)) }
            else -> emptyList()
        }

    @Test
    fun welcomeLoadingAndRetryExposeOnlyTheRelevantAction() {
        var started = 0
        var retried = 0
        val screen =
            StartupScreen(RuntimeEnvironment.getApplication(), { started++ }, { retried++ })
        fun actions() = labels(screen).filter { it.visibility == View.VISIBLE && it.isClickable }
        screen.show(welcome = true, error = false)
        assertEquals(
            listOf("Войти по QR-коду", "Войти по логину и паролю", "Продолжить как гость"),
            actions().map { it.text.toString() },
        )
        actions().last().performClick()
        assertEquals(1, started)
        screen.show(welcome = false, error = false)
        assertTrue(actions().isEmpty())
        screen.show(welcome = false, error = true)
        assertEquals(listOf("Повторить попытку"), actions().map { it.text.toString() })
        actions().single().performClick()
        assertEquals(1, retried)
        screen.show(welcome = false, error = false)
        assertTrue(actions().isEmpty())
    }
}
