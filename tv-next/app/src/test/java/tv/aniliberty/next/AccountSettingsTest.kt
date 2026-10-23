package tv.aniliberty.next

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AccountSettingsTest {
    @Test
    fun accountListsHaveIndependentRoutesAndSupportedFilters() {
        val filter =
            AccountFilter(
                years = setOf("2024", "2026"),
                sorting = "RATING_DESC",
                genres = setOf("7"),
            )
        val favorite =
            java.net.URLDecoder.decode(
                accountReleasePath(Screen.Favorites("тест", filter), 2),
                "UTF-8",
            )
        assertTrue(favorite.startsWith("/accounts/users/me/favorites/releases?"))
        assertTrue(favorite.contains("f[years]=2024,2026"))
        assertTrue(favorite.contains("f[sorting]=RATING_DESC"))
        assertTrue(favorite.contains("page=2"))
        val collection =
            java.net.URLDecoder.decode(
                accountReleasePath(Screen.Collections(CollectionType.WATCHED, "тест", filter), 1),
                "UTF-8",
            )
        assertTrue(collection.contains("/collections/releases?"))
        assertTrue(collection.contains("type_of_collection=WATCHED"))
        assertFalse(collection.contains("sorting"))
        assertFalse(collection.contains("from_year"))
    }

    @Test
    fun adPreferencesUseAuthenticatedWebsiteEndpointsAndPersistActualServerFlags() = runBlocking {
        val calls = mutableListOf<String>()
        val api =
            AccountApi(
                AccountTransport { path, method, body, token ->
                    calls += path
                    assertEquals("POST", method)
                    assertNull(body)
                    assertEquals("session", token)
                    """{"id":42,"nickname":"User","is_with_video_ads":false,"is_with_video_ads_always":true,"is_with_content_ads":false}"""
                }
            )
        val profile = api.toggleAdPreference("session", "video")
        api.toggleAdPreference("session", "always")
        api.toggleAdPreference("session", "content")
        assertEquals(false, profile.videoAds)
        assertEquals(true, profile.videoAdsAlways)
        assertEquals(false, profile.contentAds)
        assertTrue(calls[0].endsWith("toggleIsWithVideoAds"))
        assertTrue(calls[1].endsWith("toggleIsWithVideoAdsAlways"))
        assertTrue(calls[2].endsWith("toggleIsWithContentAds"))
        assertNull(parseAccountProfile(JSONObject("""{"id":42}""")).videoAds)
        val session = AccountSession(RuntimeEnvironment.getApplication())
        session.saveProfile(profile)
        assertEquals(profile, session.profile())
    }

    @Test
    fun skipUsesOnlyValidMetadataInsideTheCurrentRange() {
        assertEquals(90000L, skipTarget(EpisodeSkip(10.0, 90.0), 10000, 120000))
        assertNull(skipTarget(EpisodeSkip(10.0, 90.0), 90000, 120000))
        assertNull(skipTarget(EpisodeSkip(10.0, 90.0), 9999, 120000))
        assertNull(skipTarget(EpisodeSkip(-10.0, 90.0), 10000, 120000))
        assertNull(skipTarget(EpisodeSkip(10.0, 90.0), 10000, 60000))
        assertNull(skipTarget(EpisodeSkip(Double.NaN, 90.0), 10000, 120000))
        assertNull(skipTarget(null, 10000, 120000))
    }

    @Test
    fun tvPreferencesSurviveRecreationAndAreSeparateFromAccount() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("tv-settings", Context.MODE_PRIVATE).edit().clear().commit()
        val settings = TvSettings(context)
        assertFalse(settings.enabled("skip-opening", false))
        assertTrue(settings.toggle("skip-opening", false))
        assertTrue(TvSettings(context).enabled("skip-opening", false))
        AccountSession(context).clear()
        assertTrue(TvSettings(context).enabled("skip-opening", false))
        assertTrue(settings.enabled("menu-settings"))
    }

    @Test
    fun accountReleasesUseWrappedPaginationAndBearer() = runBlocking {
        val api =
            AccountApi(
                AccountTransport { path, method, body, token ->
                    assertTrue(path.startsWith("/accounts/users/me/favorites/releases?"))
                    assertEquals("session", token)
                    assertEquals("GET", method)
                    assertNull(body)
                    """{"data":[{"id":42,"name":{"main":"Title"},"episodes":[{"id":"e","ordinal":1}]}],"meta":{"pagination":{"current_page":1,"total_pages":2,"total":31}}}"""
                }
            )
        val page = api.releases("session", Screen.Favorites(), 1)
        assertTrue(page.more)
        assertEquals(31, page.total)
        assertEquals(1, page.releases.single().releasedEpisodes)
    }
}
