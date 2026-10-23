package tv.aniliberty.next

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DeviceLoginTest {
    @Test
    fun waitingAndTemporaryFailuresNeverExpireOrRegenerateTheCode() = runBlocking {
        var time = 0L
        var attempts = 0
        val messages = mutableListOf<String>()
        val token =
            awaitDeviceLogin(
                120000,
                now = { time },
                wait = { time += it },
                status = { messages += it },
            ) {
                attempts++
                when (attempts) {
                    1 -> throw AccountHttpException(404)
                    2 -> throw AccountHttpException(401)
                    3 -> throw java.io.IOException("offline")
                    else -> "session"
                }
            }
        assertEquals("session", token)
        assertEquals(4, attempts)
        assertEquals(20000, time)
        assertTrue(messages.any { it.contains("тем же кодом") })
    }

    @Test
    fun expiryUsesIssuedDeadlineRatherThanPollingError() = runBlocking {
        var time = 0L
        var attempts = 0
        val token =
            awaitDeviceLogin(12000, now = { time }, wait = { time += it }) {
                attempts++
                throw AccountHttpException(404)
            }
        assertNull(token)
        assertEquals(12000, time)
        assertEquals(2, attempts)
    }

    @Test
    fun closingPanelCancelsPolling() = runBlocking {
        var time = 0L
        try {
            awaitDeviceLogin(120000, now = { time }, wait = { time += it }) {
                throw CancellationException("closed")
            }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {}
    }
}
