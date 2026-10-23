package tv.aniliberty.next

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AccountApiTest {
    private data class Call(
        val path: String,
        val method: String,
        val body: String?,
        val token: String?,
    )

    @Test
    fun passwordLoginUsesDocumentedFieldsAndPreservesPassword() = runBlocking {
        var captured: Call? = null
        val api =
            AccountApi(
                AccountTransport { p, m, b, t ->
                    captured = Call(p, m, b, t)
                    "{\"token\":\"session\"}"
                }
            )
        assertEquals("session", api.login(" user ", " secret "))
        val call = requireNotNull(captured)
        assertEquals("/accounts/users/auth/login", call.path)
        assertEquals("POST", call.method)
        assertNull(call.token)
        assertEquals("user", JSONObject(call.body!!).getString("login"))
        assertEquals(" secret ", JSONObject(call.body!!).getString("password"))
    }

    @Test
    fun favoritesAndCollectionsUseSeparateAuthenticatedEndpointsAndArrayBodies() = runBlocking {
        val calls = mutableListOf<Call>()
        val api =
            AccountApi(
                AccountTransport { p, m, b, t ->
                    calls += Call(p, m, b, t)
                    "[]"
                }
            )
        api.favorite("session", 42, true)
        api.favorite("session", 42, false)
        api.collection("session", 42, CollectionType.WATCHING)
        api.collection("session", 42, null)
        assertEquals(listOf("POST", "DELETE", "POST", "DELETE"), calls.map { it.method })
        assertTrue(
            calls.all {
                it.token == "session" &&
                    JSONArray(it.body).getJSONObject(0).getInt("release_id") == 42
            }
        )
        assertTrue(calls.take(2).all { it.path.endsWith("/favorites") })
        assertEquals(
            "WATCHING",
            JSONArray(calls[2].body).getJSONObject(0).getString("type_of_collection"),
        )
        assertFalse(JSONArray(calls[3].body).getJSONObject(0).has("type_of_collection"))
    }

    @Test
    fun tupleResponsesParseWithoutTreatingCollectionAsFavorite() {
        assertEquals(setOf(42, 51), parseFavoriteIds("[42,51,42]"))
        assertEquals(
            mapOf(42 to CollectionType.WATCHING, 51 to CollectionType.PLANNED),
            parseCollectionIds("[[42,\"WATCHING\"],[51,\"PLANNED\"],[99,\"FUTURE_UNKNOWN\"]]"),
        )
        assertEquals(
            listOf(AccountTimecode("episode", 743.5, true)),
            parseTimecodes("[[\"episode\",743.5,true],[\"bad\",-1,false]]"),
        )
    }

    @Test
    fun otpPreservesLeadingZeroForDisplayAndUsesDocumentedNumericLoginCode() = runBlocking {
        val calls = mutableListOf<Call>()
        val api =
            AccountApi(
                AccountTransport { p, m, b, t ->
                    calls += Call(p, m, b, t)
                    if (p.endsWith("/get")) "{\"otp\":{\"code\":\"058701\"},\"remaining_time\":120}"
                    else "{\"token\":\"session\"}"
                }
            )
        assertEquals("058701", api.deviceCode("device").code)
        assertEquals("session", api.deviceLogin("device", "058701"))
        assertTrue(calls.all { it.token == null && it.method == "POST" })
        assertEquals(58701, JSONObject(requireNotNull(calls.last().body)).getInt("code"))
        assertEquals("device", JSONObject(requireNotNull(calls.last().body)).getString("device_id"))
    }

    @Test
    fun otpAcceptsFractionalLifetimeFromLiveApi() = runBlocking {
        val api =
            AccountApi(
                AccountTransport { _, _, _, _ ->
                    """{"otp":{"code":"058701"},"remaining_time":299.70070400000003}"""
                }
            )
        assertEquals(DeviceCode("058701", 300), api.deviceCode("device"))
    }

    @Test
    fun timecodeWritesUseSecondsAndWatchedFlagWithoutReleasePositionIndex() = runBlocking {
        var sent = ""
        val api =
            AccountApi(
                AccountTransport { p, m, b, t ->
                    assertEquals("/accounts/users/me/views/timecodes", p)
                    assertEquals("POST", m)
                    assertEquals("session", t)
                    sent = b!!
                    ""
                }
            )
        api.updateTimecodes("session", listOf(AccountTimecode("episode-uuid", 12.375, false)))
        val value = JSONArray(sent).getJSONObject(0)
        assertEquals("episode-uuid", value.getString("release_episode_id"))
        assertEquals(12.375, value.getDouble("time"), .00001)
        assertFalse(value.getBoolean("is_watched"))
    }

    @Test
    fun localHistoryImportExcludesUnstartedAndKeepsFractionalSeconds() {
        val prefs =
            RuntimeEnvironment.getApplication()
                .getSharedPreferences("import-test", Context.MODE_PRIVATE)
        prefs
            .edit()
            .clear()
            .putLong("position:one", 12375)
            .putLong("duration:one", 50000)
            .putLong("position:two", 0)
            .putLong("position:done", 60000)
            .putLong("duration:done", 60000)
            .commit()
        val records = localTimecodes(prefs).associateBy { it.episodeId }
        assertEquals(setOf("one", "done"), records.keys)
        assertEquals(12.375, records.getValue("one").seconds, .00001)
        assertTrue(records.getValue("done").watched)
        assertFalse(records.getValue("one").watched)
        assertEquals(
            listOf(records.getValue("done")),
            importCandidates(records.values.toList(), setOf("one")),
        )
    }

    @Test
    fun accountHistoryUsesNestedReleaseAndEpisodeIds() {
        val items =
            parseAccountHistory(
                """{"data":[{"release_episode_id":"uuid","updated_at":"2026-10-22T01:00:00Z","release_episode":{"id":"uuid","release":{"id":42}}},{"release_episode_id":"deleted"}]}"""
            )
        assertEquals(1, items.size)
        assertEquals(42, items.single().releaseId)
        assertEquals("uuid", items.single().episodeId)
        assertTrue(items.single().updatedAt > 0)
    }

    @Test
    fun profileIncludesIdentityAndHistoryPageIncludesPlayableEpisodeContext() {
        val profile =
            parseAccountProfile(
                JSONObject(
                    """{"id":42,"nickname":"Иван","login":"ivan","email":"user@example.test","created_at":"2020-01-01T00:00:00Z","avatar":{"preview":"/avatar.webp"}}"""
                )
            )
        assertEquals("Иван", profile.name)
        assertEquals("ivan", profile.login)
        assertEquals("user@example.test", profile.email)
        assertNotNull(profile.avatar)
        val page =
            parseAccountHistoryPage(
                """{"data":[{"time":12.5,"is_watched":false,"release_episode":{"id":"uuid","ordinal":2,"duration":1200,"release":{"id":42,"name":{"main":"Тайтл"}}}}],"meta":{"pagination":{"total_pages":2}}}""",
                1,
            )
        assertEquals(42, page.items.single().release.id)
        assertEquals("uuid", page.items.single().episode.id)
        assertEquals(12.5, page.items.single().time.seconds, .001)
        assertTrue(page.more)
    }

    @Test
    fun identityAndSessionManagementFollowOfficialWebClientEndpoints() = runBlocking {
        val calls = mutableListOf<Call>()
        val api =
            AccountApi(
                AccountTransport { p, m, b, t ->
                    calls += Call(p, m, b, t)
                    if (p.contains("updateNickname")) """{"id":42,"nickname":"Новое имя"}"""
                    else "[]"
                }
            )
        assertEquals("Новое имя", api.updateIdentity("session", true, "Новое имя").name)
        api.deleteSession("session", "opaque-id")
        api.deleteOtherSessions("session")
        assertEquals(
            "Новое имя",
            JSONObject(requireNotNull(calls.first().body)).getString("new_nickname"),
        )
        assertTrue(calls.first().path.endsWith("/updateNickname"))
        assertEquals("POST", calls.first().method)
        assertTrue(calls[1].path.endsWith("/deleteUserSession/opaque-id"))
        assertEquals("DELETE", calls[1].method)
        assertTrue(calls[2].path.endsWith("/deleteAllOtherSessions"))
        assertEquals("DELETE", calls[2].method)
        assertTrue(calls.all { it.token == "session" })
    }

    @Test
    fun currentDeviceIsIdentifiedAndOrderedFirst() {
        val sessions =
            parseAccountSessions(
                """[{"id":"other","device":{"name":"Phone"},"is_current":false},{"id":"this","device":{"name":"Android TV"},"is_current":true}]"""
            )
        assertEquals("this", sessions.first().id)
        assertTrue(sessions.first().current)
        assertFalse(sessions.last().current)
    }

    @Test
    fun guestAndAccountPlaybackAreIsolatedAndUnsentQueueIsDurable() {
        val context = RuntimeEnvironment.getApplication()
        val session = context.getSharedPreferences("account-session", Context.MODE_PRIVATE)
        session.edit().clear().commit()
        val guest = playbackPreferences(context)
        guest.edit().clear().putLong("position:ep", 100).commit()
        session.edit().putInt("user", 123).commit()
        val signedIn = playbackPreferences(context)
        signedIn.edit().clear().commit()
        assertEquals(0L, signedIn.getLong("position:ep", 0))
        queueAccountProgress(context, "ep", 12345, 20000)
        assertEquals(12.345, pending(signedIn).getValue("ep").seconds, .00001)
        session.edit().putInt("user", 456).commit()
        assertTrue(pending(playbackPreferences(context)).isEmpty())
        session.edit().clear().commit()
        assertEquals(100L, playbackPreferences(context).getLong("position:ep", 0))
    }
}
