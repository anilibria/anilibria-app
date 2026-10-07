package ru.radiationx.anilibria.common

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.radiationx.data.entity.domain.release.EpisodeAccess
import ru.radiationx.data.entity.domain.types.EpisodeId
import ru.radiationx.data.entity.domain.types.ReleaseId

class PosterWatchedStateTest {
    private val first = EpisodeId("1", ReleaseId(1))
    private val second = EpisodeId("2", ReleaseId(1))

    @Test
    fun `no viewed marks avoids loading playlists`() = runBlocking {
        var loads = 0
        val accesses = MutableStateFlow(listOf(mark(first, false, 5000)))
        assertFalse(posterWatchedState(accesses) {
            loads++
            MutableStateFlow(listOf(first))
        }.first())
        assertEquals(0, loads)
    }

    @Test
    fun `manual marks without progress count as viewed`() = runBlocking {
        assertTrue(state(listOf(first, second), listOf(mark(first), mark(second))))
    }

    @Test
    fun `empty or unknown playlist never shows stripe`() = runBlocking {
        assertFalse(state(emptyList(), listOf(mark(first))))
    }

    @Test
    fun `missing mark and foreign or stale marks do not complete release`() = runBlocking {
        assertFalse(state(listOf(first, second), listOf(
            mark(first), mark(EpisodeId("3", ReleaseId(1))), mark(EpisodeId("2", ReleaseId(2))),
        )))
    }

    @Test
    fun `partial progress alone cannot complete release`() = runBlocking {
        assertFalse(state(listOf(first, second), listOf(mark(first), mark(second, false, 5000))))
    }

    @Test
    fun `new episode restarting and history reset update visible state`() = runBlocking {
        withTimeout(5000) {
            val episodes = MutableStateFlow(listOf(first))
            val accesses = MutableStateFlow(listOf(mark(first)))
            val results = Channel<Boolean>(Channel.UNLIMITED)
            val job = launch { posterWatchedState(accesses) { episodes }.collect { results.send(it) } }
            try {
                assertTrue(results.receive())
                episodes.value = listOf(first, second)
                assertFalse(results.receive())
                accesses.value = listOf(mark(first), mark(second))
                assertTrue(results.receive())
                accesses.value = listOf(mark(first, false), mark(second))
                assertFalse(results.receive())
                accesses.value = listOf(mark(first), mark(second))
                assertTrue(results.receive())
                accesses.value = emptyList()
                assertFalse(results.receive())
                accesses.value = listOf(mark(first), mark(second))
                assertTrue(results.receive())
            } finally {
                job.cancelAndJoin()
            }
        }
    }

    private suspend fun state(ids: List<EpisodeId>, marks: List<EpisodeAccess>) =
        posterWatchedState(MutableStateFlow(marks)) { MutableStateFlow(ids) }.first()

    private fun mark(id: EpisodeId, viewed: Boolean = true, seek: Long = 0) =
        EpisodeAccess(id, seek, viewed, 0)
}
