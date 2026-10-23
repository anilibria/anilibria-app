package tv.aniliberty.next

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class LocalLibraryTest {
    private val context
        get() = RuntimeEnvironment.getApplication()

    @Before
    fun clear() {
        context.getSharedPreferences("viewing", Context.MODE_PRIVATE).edit().clear().commit()
        context
            .getSharedPreferences("account-session", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    private fun release(id: Int, title: String) =
        parseRelease(
            JSONObject(
                """{"id":$id,"name":{"main":"$title"},"year":2026,"genres":[{"name":"Комедия"}]}"""
            )
        )

    @Test
    fun guestFavoritesAndAllFiveCollectionsPersistWithoutRequests() = runBlocking {
        val repository =
            AccountRepository(
                context,
                AccountApi(
                    AccountTransport { _, _, _, _ -> error("Guest must not call account API") }
                ),
            )
        CollectionType.entries.forEachIndexed { index, type ->
            val item = release(index + 1, type.title)
            repository.local.remember(item)
            repository.collection(item.id, type)
            repository.favorite(item.id, true)
        }
        val recreated = AccountRepository(context)
        assertFalse(recreated.signedIn)
        assertEquals(5, recreated.lists!!.favorites.size)
        CollectionType.entries.forEach { type ->
            val items = recreated.releases(Screen.Collections(type), 1).releases
            assertEquals(type.title, items.single().title)
        }
        repository.favorite(1, false)
        repository.collection(2, null)
        assertFalse(1 in repository.lists!!.favorites)
        assertEquals(CollectionType.PLANNED, repository.lists!!.collections[1])
        assertTrue(2 in repository.lists!!.favorites)
        assertFalse(2 in repository.lists!!.collections)
    }

    @Test
    fun oldLocalLibraryIsPreservedAndSearchUsesSavedMetadata() = runBlocking {
        context
            .getSharedPreferences("viewing", Context.MODE_PRIVATE)
            .edit()
            .putString("library", """[{"id":42,"title":"Старый тайтл"}]""")
            .commit()
        val repository = AccountRepository(context)
        assertEquals(setOf(42), repository.lists!!.favorites)
        assertEquals(
            "Старый тайтл",
            repository.releases(Screen.Favorites("старый"), 1).releases.single().title,
        )
        repository.local.remember(release(43, "Новый"))
        repository.favorite(43, true)
        assertEquals(
            listOf(43),
            repository
                .releases(Screen.Favorites(filter = AccountFilter(years = setOf("2026"))), 1)
                .releases
                .map { it.id },
        )
        assertEquals(setOf(42, 43), AccountRepository(context).lists!!.favorites)
    }

    @Test
    fun guestHistoryAndTimecodesStaySeparateFromAccountStorage() = runBlocking {
        val prefs = context.getSharedPreferences("viewing", Context.MODE_PRIVATE)
        val release = release(7, "История")
        val episode = Episode("uuid", 1.0, "Серия", null, 120, emptyList(), 1.0)
        ViewingHistory(prefs).save(release, episode, 55000, 120000)
        prefs.edit().putLong("position:uuid", 55000).putLong("duration:uuid", 120000).commit()
        val repository =
            AccountRepository(
                context,
                AccountApi(AccountTransport { _, _, _, _ -> error("Guest history must be local") }),
            )
        val item = repository.history().items.single()
        assertEquals("История", item.release.title)
        assertEquals(55.0, item.time.seconds, 0.0)
        assertEquals(1, localTimecodes(prefs).size)
        assertTrue(
            context.getSharedPreferences("viewing-account-42", Context.MODE_PRIVATE).all.isEmpty()
        )
        assertEquals(
            emptyList<AccountTimecode>(),
            importCandidates(localTimecodes(prefs), setOf("uuid")),
        )
    }
}
