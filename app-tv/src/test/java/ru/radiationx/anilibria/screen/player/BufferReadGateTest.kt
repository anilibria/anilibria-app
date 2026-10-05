package ru.radiationx.anilibria.screen.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.InterruptedIOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class BufferReadGateTest {
    @Test(timeout = 5000)
    fun `a full buffer blocks loading until playback releases memory`() {
        val allocated = AtomicInteger(1024)
        val observed = CountDownLatch(1)
        val completed = CountDownLatch(1)
        val gate = BufferReadGate(1024, { observed.countDown(); allocated.get() }, { false })
        val worker = Thread { gate.awaitCapacity(); completed.countDown() }
        worker.start()
        try {
            assertTrue(observed.await(1, TimeUnit.SECONDS))
            assertFalse(completed.await(100, TimeUnit.MILLISECONDS))
            allocated.set(512)
            assertTrue(completed.await(1, TimeUnit.SECONDS))
            assertEquals(1, gate.waitCount.get())
        } finally {
            worker.interrupt()
            worker.join(1000)
        }
    }

    @Test(timeout = 5000)
    fun `seek or release interruption cancels a blocked read`() {
        val observed = CountDownLatch(1)
        val result = AtomicReference<Throwable>()
        val interrupted = AtomicBoolean()
        val gate = BufferReadGate(1024, { observed.countDown(); 1024 }, { true })
        val worker = Thread {
            try {
                gate.awaitCapacity()
            } catch (error: Throwable) {
                result.set(error)
                interrupted.set(Thread.currentThread().isInterrupted)
            }
        }
        worker.start()
        try {
            assertTrue(observed.await(1, TimeUnit.SECONDS))
            worker.interrupt()
            worker.join(1000)
            assertFalse(worker.isAlive)
            assertTrue(result.get() is InterruptedIOException)
            assertTrue(interrupted.get())
        } finally {
            worker.interrupt()
            worker.join(1000)
        }
    }

    @Test(timeout = 5000)
    fun `paused playback can keep a full buffer without a stall error`() {
        val allocated = AtomicInteger(1024)
        val observed = CountDownLatch(1)
        val result = AtomicReference<Throwable>()
        val completed = CountDownLatch(1)
        val gate = BufferReadGate(1024, { observed.countDown(); allocated.get() }, { true }, 40)
        val worker = Thread {
            try { gate.awaitCapacity() } catch (error: Throwable) { result.set(error) }
            completed.countDown()
        }
        worker.start()
        try {
            assertTrue(observed.await(1, TimeUnit.SECONDS))
            assertFalse(completed.await(150, TimeUnit.MILLISECONDS))
            allocated.set(0)
            assertTrue(completed.await(1, TimeUnit.SECONDS))
            assertEquals(null, result.get())
        } finally {
            worker.interrupt()
            worker.join(1000)
        }
    }

    @Test(timeout = 5000, expected = BufferCapacityException::class)
    fun `a stream unable to free samples fails instead of waiting forever`() {
        BufferReadGate(1024, { 1024 }, { false }, 40).awaitCapacity()
    }
}
