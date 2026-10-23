package tv.aniliberty.next

import android.app.Activity
import android.os.Looper
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.widget.Button
import java.time.Duration
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class RemoteInputTest {
    private fun key(
        action: Int,
        time: Long = 0,
        repeat: Int = 0,
        code: Int = KeyEvent.KEYCODE_DPAD_CENTER,
    ) = KeyEvent(0, time, action, code, repeat)

    @Test
    fun shortOkClicksOnceAndRepeatedKeyUpDoesNothing() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup().visible()
        val button = Button(controller.get())
        controller.get().setContentView(button)
        button.requestFocus()
        var count = 0
        button.setOnClickListener { count++ }
        val input = RemoteClick()
        assertTrue(input.handle(key(KeyEvent.ACTION_DOWN), button))
        input.handle(key(KeyEvent.ACTION_DOWN, 100, 1), button)
        input.handle(key(KeyEvent.ACTION_UP, 150), button)
        input.handle(key(KeyEvent.ACTION_UP, 160), button)
        assertEquals(1, count)
        assertFalse(button.isPressed)
        controller.pause().stop().destroy()
    }

    @Test
    fun longOkNeverFollowsWithShortClickAndCancellationStopsPendingHold() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup().visible()
        val button = Button(controller.get())
        controller.get().setContentView(button)
        button.requestFocus()
        var clicks = 0
        var holds = 0
        button.setOnClickListener { clicks++ }
        button.setOnLongClickListener {
            holds++
            true
        }
        val input = RemoteClick()
        input.handle(key(KeyEvent.ACTION_DOWN), button)
        shadowOf(Looper.getMainLooper())
            .idleFor(Duration.ofMillis(ViewConfiguration.getLongPressTimeout() + 10L))
        input.handle(key(KeyEvent.ACTION_UP, 1000), button)
        assertEquals(1, holds)
        assertEquals(0, clicks)
        input.handle(key(KeyEvent.ACTION_DOWN), button)
        input.cancel()
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(1000))
        input.handle(key(KeyEvent.ACTION_UP, 1000), button)
        assertEquals(1, holds)
        assertEquals(0, clicks)
        controller.pause().stop().destroy()
    }

    @Test
    fun repeatLimiterNeverDropsFreshPressesOrDirectionChanges() {
        val gate = RemoteRepeat()
        assertFalse(gate.discard(key(KeyEvent.ACTION_DOWN, 0, 0, KeyEvent.KEYCODE_DPAD_RIGHT)))
        assertTrue(gate.discard(key(KeyEvent.ACTION_DOWN, 30, 1, KeyEvent.KEYCODE_DPAD_RIGHT)))
        assertFalse(gate.discard(key(KeyEvent.ACTION_DOWN, 100, 2, KeyEvent.KEYCODE_DPAD_RIGHT)))
        assertFalse(gate.discard(key(KeyEvent.ACTION_DOWN, 110, 0, KeyEvent.KEYCODE_DPAD_LEFT)))
        assertFalse(gate.discard(key(KeyEvent.ACTION_UP, 120, 0, KeyEvent.KEYCODE_DPAD_LEFT)))
        assertFalse(gate.discard(key(KeyEvent.ACTION_DOWN, 125, 0, KeyEvent.KEYCODE_DPAD_LEFT)))
    }

    @Test
    fun seekAcceleratesByHeldTimeAndHonorsMediaBounds() {
        assertEquals(20000L, remoteSeekTarget(10000, 120000, true, 0))
        assertEquals(30000L, remoteSeekTarget(10000, 120000, true, 1000))
        assertEquals(40000L, remoteSeekTarget(10000, 120000, true, 3000))
        assertEquals(0L, remoteSeekTarget(1000, 120000, false, 3000))
        assertEquals(120000L, remoteSeekTarget(119000, 120000, true, 0))
        assertNull(remoteSeekTarget(0, 0, true, 0))
    }
}
