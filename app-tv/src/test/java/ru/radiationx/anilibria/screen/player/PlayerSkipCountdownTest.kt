package ru.radiationx.anilibria.screen.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerSkipCountdownTest {
    private val timer = PlayerSkipCountdown()

    @Test
    fun `appearance and initial buffering consume none of the five seconds`() {
        timer.setRunning(false, 0)
        timer.setRunning(false, 30_000)
        timer.setRunning(true, 30_250)
        assertEquals(5_000L, timer.remainingMs)
        timer.setRunning(true, 31_250)
        assertEquals(4L, timer.remainingSeconds)
    }

    @Test
    fun `pause resumes the remaining fraction instead of restarting`() {
        timer.setRunning(true, 0)
        timer.setRunning(false, 1_250)
        timer.setRunning(false, 61_250)
        assertEquals(3_750L, timer.remainingMs)
        timer.setRunning(true, 61_250)
        timer.setRunning(true, 62_250)
        assertEquals(2_750L, timer.remainingMs)
    }

    @Test
    fun `Watch focus can freeze longer than the entire countdown`() {
        timer.setRunning(true, 0)
        timer.setRunning(false, 2_000)
        timer.setRunning(false, 12_000)
        assertEquals(3L, timer.remainingSeconds)
        assertFalse(timer.isFinished)
        timer.setRunning(true, 12_000)
        timer.setRunning(true, 14_999)
        assertFalse(timer.isFinished)
        timer.setRunning(true, 15_000)
        assertTrue(timer.isFinished)
    }

    @Test
    fun `buffering and another appearance do not spend remaining time`() {
        timer.setRunning(true, 0)
        timer.setRunning(false, 750)
        timer.setRunning(false, 20_000)
        timer.setRunning(true, 20_250)
        assertEquals(4_250L, timer.remainingMs)
        assertEquals(5L, timer.remainingSeconds)
    }

    @Test
    fun `another prompt receives a fresh countdown`() {
        timer.setRunning(true, 0)
        timer.setRunning(true, 4_000)
        timer.reset()
        timer.setRunning(false, 10_000)
        assertEquals(5_000L, timer.remainingMs)
        timer.setRunning(true, 10_000)
        timer.setRunning(true, 15_000)
        assertTrue(timer.isFinished)
    }
}
