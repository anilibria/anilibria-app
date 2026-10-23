package tv.aniliberty.next

import org.json.JSONArray
import org.json.JSONObject

class AccountApi(private val transport: AccountTransport = AccountHttpTransport()) {
    suspend fun login(login: String, password: String): String {
        require(login.isNotBlank() && password.isNotEmpty()) { "Введите логин и пароль" }
        return JSONObject(
                transport.request(
                    "/accounts/users/auth/login",
                    "POST",
                    JSONObject().put("login", login.trim()).put("password", password).toString(),
                    null,
                )
            )
            .getString("token")
            .also { require(it.isNotBlank()) { "Сервис не вернул токен" } }
    }

    suspend fun deviceCode(deviceId: String): DeviceCode {
        val json =
            JSONObject(
                transport.request(
                    "/accounts/otp/get",
                    "POST",
                    JSONObject().put("device_id", deviceId).toString(),
                    null,
                )
            )
        val code = json.getJSONObject("otp").getString("code")
        require(Regex("[0-9]{6}").matches(code)) { "Сервис вернул некорректный код" }
        return DeviceCode(
            code,
            kotlin.math.ceil(json.getDouble("remaining_time")).toInt().coerceIn(1, 600),
        )
    }

    suspend fun deviceLogin(deviceId: String, code: String): String =
        JSONObject(
                transport.request(
                    "/accounts/otp/login",
                    "POST",
                    JSONObject().put("device_id", deviceId).put("code", code.toInt()).toString(),
                    null,
                )
            )
            .getString("token")
            .also { require(it.isNotBlank()) }

    suspend fun profile(token: String): AccountProfile {
        val json =
            JSONObject(
                transport.request("/accounts/users/me/profile?exclude=torrents", "GET", null, token)
            )
        return parseAccountProfile(json)
    }

    suspend fun lists(token: String): AccountLists {
        val favorites =
            parseFavoriteIds(
                transport.request("/accounts/users/me/favorites/ids", "GET", null, token)
            )
        val collections =
            parseCollectionIds(
                transport.request("/accounts/users/me/collections/ids", "GET", null, token)
            )
        return AccountLists(favorites, collections)
    }

    suspend fun favorite(token: String, id: Int, added: Boolean) {
        transport.request(
            "/accounts/users/me/favorites",
            if (added) "POST" else "DELETE",
            releaseBody(id).toString(),
            token,
        )
    }

    suspend fun collection(token: String, id: Int, type: CollectionType?) {
        val body = releaseBody(id)
        if (type != null) body.getJSONObject(0).put("type_of_collection", type.name)
        transport.request(
            "/accounts/users/me/collections",
            if (type == null) "DELETE" else "POST",
            body.toString(),
            token,
        )
    }

    suspend fun timecodes(token: String): List<AccountTimecode> =
        parseTimecodes(transport.request("/accounts/users/me/views/timecodes", "GET", null, token))

    suspend fun updateTimecodes(token: String, values: List<AccountTimecode>) {
        if (values.isEmpty()) return
        val body = JSONArray()
        values.forEach { v ->
            body.put(
                JSONObject()
                    .put("release_episode_id", v.episodeId)
                    .put("time", v.seconds.coerceAtLeast(0.0))
                    .put("is_watched", v.watched)
            )
        }
        transport.request("/accounts/users/me/views/timecodes", "POST", body.toString(), token)
    }

    suspend fun releases(token: String, screen: Screen, page: Int): CatalogPage =
        parseCatalogPage(
            transport.request(accountReleasePath(screen, page), "GET", null, token),
            page,
        )

    suspend fun references(token: String, favorites: Boolean): Map<String, List<CatalogChoice>> {
        val section = if (favorites) "favorites" else "collections"
        val keys =
            listOf("genres", "types", "years", "age-ratings") +
                if (favorites) listOf("sorting") else emptyList()
        return keys.associateWith { key ->
            parseCatalogChoices(
                transport.request(
                    "/accounts/users/me/$section/references/$key",
                    "GET",
                    null,
                    token,
                ),
                key == "years",
            )
        }
    }

    suspend fun toggleAdPreference(token: String, key: String): AccountProfile {
        val method =
            when (key) {
                "video" -> "toggleIsWithVideoAds"
                "always" -> "toggleIsWithVideoAdsAlways"
                "content" -> "toggleIsWithContentAds"
                else -> error("Неизвестная настройка")
            }
        val response =
            transport.request("/accounts/users/me/settings/site/$method", "POST", null, token)
        return if (response.isNotBlank()) parseAccountProfile(JSONObject(response))
        else profile(token)
    }

    suspend fun updateIdentity(token: String, nickname: Boolean, value: String): AccountProfile {
        val field = if (nickname) "Nickname" else "Login"
        val body = JSONObject().put(if (nickname) "new_nickname" else "new_login", value)
        val response =
            transport.request(
                "/accounts/users/me/settings/account/update$field",
                "POST",
                body.toString(),
                token,
            )
        return if (response.isNotBlank()) parseAccountProfile(JSONObject(response))
        else profile(token)
    }

    suspend fun sessions(token: String): List<AccountDeviceSession> =
        parseAccountSessions(
            transport.request(
                "/accounts/users/me/settings/sessions/fetchUserSessions",
                "GET",
                null,
                token,
            )
        )

    suspend fun deleteSession(token: String, id: String) {
        transport.request(
            "/accounts/users/me/settings/sessions/deleteUserSession/${java.net.URLEncoder.encode(id,"UTF-8")}",
            "DELETE",
            null,
            token,
        )
    }

    suspend fun deleteOtherSessions(token: String) {
        transport.request(
            "/accounts/users/me/settings/sessions/deleteAllOtherSessions",
            "DELETE",
            null,
            token,
        )
    }

    suspend fun history(token: String, page: Int = 1): AccountHistoryPage =
        parseAccountHistoryPage(
            transport.request(
                "/accounts/users/me/views/history?limit=30&page=$page",
                "GET",
                null,
                token,
            ),
            page,
        )

    suspend fun recentHistory(token: String): List<AccountHistoryItem> =
        parseAccountHistory(
            transport.request("/accounts/users/me/views/history?limit=30", "GET", null, token)
        )

    suspend fun logout(token: String) {
        transport.request("/accounts/users/auth/logout", "POST", null, token)
    }

    private fun releaseBody(id: Int) = JSONArray().put(JSONObject().put("release_id", id))
}

internal fun parseFavoriteIds(body: String): Set<Int> {
    val json = JSONArray(body)
    return (0 until json.length()).mapNotNull { json.optInt(it, -1).takeIf { it > 0 } }.toSet()
}

internal fun parseCollectionIds(body: String): Map<Int, CollectionType> {
    val json = JSONArray(body)
    return (0 until json.length())
        .mapNotNull { i ->
            val pair = json.optJSONArray(i) ?: return@mapNotNull null
            val id = pair.optInt(0, -1)
            val type = CollectionType.entries.firstOrNull { it.name == pair.optString(1) }
            if (id > 0 && type != null) id to type else null
        }
        .toMap()
}

internal fun parseTimecodes(body: String): List<AccountTimecode> {
    val json = JSONArray(body)
    return (0 until json.length()).mapNotNull { i ->
        val tuple = json.optJSONArray(i) ?: return@mapNotNull null
        val id = tuple.optString(0)
        val seconds = tuple.optDouble(1, Double.NaN)
        if (id.isBlank() || !seconds.isFinite() || seconds < 0) null
        else AccountTimecode(id, seconds, tuple.optBoolean(2))
    }
}

internal fun parseAccountHistory(body: String): List<AccountHistoryItem> {
    val data = JSONObject(body).optJSONArray("data") ?: return emptyList()
    return (0 until data.length())
        .mapNotNull { i ->
            val item = data.optJSONObject(i) ?: return@mapNotNull null
            val episode = item.optJSONObject("release_episode") ?: return@mapNotNull null
            val release = episode.optJSONObject("release")?.optInt("id", -1) ?: -1
            val id = item.optString("release_episode_id").ifBlank { episode.optString("id") }
            val updated =
                runCatching {
                        java.time.OffsetDateTime.parse(item.optString("updated_at"))
                            .toInstant()
                            .toEpochMilli()
                    }
                    .getOrDefault(0)
            if (release > 0 && id.isNotBlank()) AccountHistoryItem(release, id, updated) else null
        }
        .sortedByDescending { it.updatedAt }
}

internal fun parseAccountProfile(json: JSONObject) =
    AccountProfile(
        json.getInt("id"),
        json.optString("nickname").ifBlank {
            json.optString("login").ifBlank { "Аккаунт AniLiberty" }
        },
        json.optString("login"),
        json.optString("email"),
        json.optJSONObject("avatar")?.let {
            imageUrl(it.optJSONObject("optimized")?.optString("preview"))
                ?: imageUrl(it.optString("preview"))
        },
        json.optString("created_at"),
        json.optBoolean("is_banned"),
        json.accountBoolean("is_with_video_ads"),
        json.accountBoolean("is_with_video_ads_always"),
        json.accountBoolean("is_with_content_ads"),
    )

data class AccountViewedEpisode(
    val release: Release,
    val episode: Episode,
    val time: AccountTimecode,
    val updatedAt: String,
)

data class AccountHistoryPage(val items: List<AccountViewedEpisode>, val more: Boolean)

internal fun parseAccountHistoryPage(body: String, page: Int): AccountHistoryPage {
    val json = JSONObject(body)
    val data = json.optJSONArray("data") ?: JSONArray()
    val items =
        (0 until data.length()).mapNotNull { i ->
            val entry = data.optJSONObject(i) ?: return@mapNotNull null
            val e = entry.optJSONObject("release_episode") ?: return@mapNotNull null
            val r = e.optJSONObject("release") ?: return@mapNotNull null
            val release =
                runCatching {
                        parseRelease(JSONObject(r.toString()).put("episodes", JSONArray().put(e)))
                    }
                    .getOrNull() ?: return@mapNotNull null
            val episode = release.episodes.firstOrNull() ?: return@mapNotNull null
            AccountViewedEpisode(
                release,
                episode,
                AccountTimecode(
                    episode.id,
                    entry.optDouble("time", 0.0).coerceAtLeast(0.0),
                    entry.optBoolean("is_watched"),
                ),
                entry.optString("updated_at"),
            )
        }
    require(data.length() == 0 || items.isNotEmpty()) {
        "Не удалось прочитать серии в истории аккаунта"
    }
    val pagination = json.optJSONObject("meta")?.optJSONObject("pagination")
    val pages = pagination?.optInt("total_pages", -1) ?: -1
    return AccountHistoryPage(items, if (pages >= 0) page < pages else data.length() >= 30)
}

data class AccountDeviceSession(
    val id: String,
    val title: String,
    val description: String,
    val current: Boolean,
)

internal fun parseAccountSessions(body: String): List<AccountDeviceSession> {
    val data = JSONArray(body)
    return (0 until data.length())
        .mapNotNull { i ->
            val item = data.optJSONObject(i) ?: return@mapNotNull null
            val id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val device = item.optJSONObject("device")
            val browser = item.optJSONObject("browser")
            val title =
                listOf(device?.optString("name").orEmpty(), browser?.optString("name").orEmpty())
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(" · ")
                    .ifBlank { "Устройство" }
            val description =
                listOf(
                        device?.optString("platform").orEmpty(),
                        item.optJSONObject("location")?.optString("country").orEmpty(),
                        updateTimestamp(item.optString("last_active")),
                    )
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
            AccountDeviceSession(id, title, description, item.optBoolean("is_current"))
        }
        .sortedByDescending { it.current }
}

private fun JSONObject.accountBoolean(key: String): Boolean? =
    if (has(key) && !isNull(key)) getBoolean(key) else null
