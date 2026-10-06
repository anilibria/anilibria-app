package ru.radiationx.anilibria.screen.details

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.radiationx.data.entity.domain.release.BlockedInfo
import ru.radiationx.data.entity.domain.release.FavoriteInfo
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.types.ReleaseCode
import ru.radiationx.data.entity.domain.types.ReleaseId

class DetailReleaseFlowTest {

    @Test
    fun `adding survives stale reload and reopening`() = runBlocking {
        verifyChange(false, true)
    }

    @Test
    fun `removing survives stale reload and reopening`() = runBlocking {
        verifyChange(true, false)
    }

    @Test
    fun `without local action server state is used`() = runBlocking {
        val release = release(true)
        assertSame(release, MutableStateFlow(release)
            .withFavoriteChanges(MutableStateFlow(null)).first())
    }

    @Test
    fun `clearing account override restores server state`() = runBlocking {
        withTimeout(5000) {
            val changes = MutableStateFlow<Boolean?>(true)
            val releases = MutableStateFlow(release(false))
            val results = Channel<Release>(Channel.UNLIMITED)
            val job = launch { releases.withFavoriteChanges(changes).collect { results.send(it) } }
            try {
                assertTrue(results.receive().favoriteInfo.isAdded)
                changes.value = null
                assertFalse(results.receive().favoriteInfo.isAdded)
            } finally {
                job.cancelAndJoin()
            }
        }
    }

    private suspend fun verifyChange(initial: Boolean, changed: Boolean) {
        withTimeout(5000) {
            val releases = MutableStateFlow(release(initial))
            val changes = MutableStateFlow<Boolean?>(null)
            val results = Channel<Release>(Channel.UNLIMITED)
            val job = launch { releases.withFavoriteChanges(changes).collect { results.send(it) } }
            try {
                assertEquals(initial, results.receive().favoriteInfo.isAdded)
                changes.value = changed
                assertEquals(changed, results.receive().favoriteInfo.isAdded)
                // The next full response still has the old flag, but newer release metadata.
                val refreshed = release(initial).copy(names = listOf("Updated title"),
                    favoriteInfo = FavoriteInfo(101, initial))
                releases.value = refreshed
                val updated = results.receive()
                assertEquals(changed, updated.favoriteInfo.isAdded)
                assertEquals(101, updated.favoriteInfo.rating)
                assertEquals(refreshed.names, updated.names)
                job.cancelAndJoin()
                // A new card subscribes to the same repository flows after navigating back.
                assertEquals(changed, releases.withFavoriteChanges(changes).first().favoriteInfo.isAdded)
                changes.value = !changed
                assertEquals(!changed, releases.withFavoriteChanges(changes).first().favoriteInfo.isAdded)
            } finally {
                job.cancelAndJoin()
            }
        }
    }

    private fun release(isFavorite: Boolean) = Release(
        id = ReleaseId(1), code = ReleaseCode("test"), names = listOf("Title"),
        series = null, poster = null, torrentUpdate = 0, status = null, statusCode = null,
        types = emptyList(), genres = emptyList(), voices = emptyList(), members = null,
        year = null, season = null, days = emptyList(), description = null, announce = null,
        favoriteInfo = FavoriteInfo(100, isFavorite), link = null, franchises = emptyList(),
        showDonateDialog = false, blockedInfo = BlockedInfo(false, null), moonwalkLink = null,
        episodes = emptyList(), sourceEpisodes = emptyList(), externalPlaylists = emptyList(),
        rutubePlaylist = emptyList(), torrents = emptyList()
    )
}
