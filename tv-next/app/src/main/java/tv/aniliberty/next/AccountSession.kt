package tv.aniliberty.next

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

/** Token is encrypted with an Android Keystore key; account data and guest data remain separate. */
class AccountSession(context: Context) {
    private val prefs = context.getSharedPreferences("account-session", Context.MODE_PRIVATE)
    val userId: Int?
        get() = prefs.getInt("user", 0).takeIf { it > 0 }

    val name: String
        get() = prefs.getString("name", "").orEmpty()

    fun profile(): AccountProfile? =
        runCatching {
                prefs.getString("profile", null)?.let { parseAccountProfile(JSONObject(it)) }
            }
            .getOrNull()

    val deviceId: String
        get() =
            prefs.getString("device", null)
                ?: UUID.randomUUID().toString().also {
                    prefs.edit().putString("device", it).apply()
                }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (store.getKey("aniliberty-account", null) as? SecretKey)
            ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                .apply {
                    init(
                        KeyGenParameterSpec.Builder(
                                "aniliberty-account",
                                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                            )
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build()
                    )
                }
                .generateKey()
    }

    fun token(): String? =
        runCatching {
                val packed = prefs.getString("session", null) ?: return null
                val parts = packed.split(":")
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    key(),
                    GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)),
                )
                String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8)
            }
            .getOrNull()

    fun save(token: String, profile: AccountProfile) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val packed =
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP) +
                ":" +
                Base64.encodeToString(
                    cipher.doFinal(token.toByteArray(Charsets.UTF_8)),
                    Base64.NO_WRAP,
                )
        prefs
            .edit()
            .putString("session", packed)
            .putInt("user", profile.id)
            .putString("name", profile.name)
            .apply()
        saveProfile(profile)
    }

    fun saveProfile(profile: AccountProfile) {
        val json =
            JSONObject()
                .put("id", profile.id)
                .put("nickname", profile.name)
                .put("login", profile.login)
                .put("email", profile.email)
                .put("created_at", profile.createdAt)
                .put("is_banned", profile.banned)
        profile.videoAds?.let { json.put("is_with_video_ads", it) }
        profile.videoAdsAlways?.let { json.put("is_with_video_ads_always", it) }
        profile.contentAds?.let { json.put("is_with_content_ads", it) }
        profile.avatar?.let { json.put("avatar", JSONObject().put("preview", it)) }
        prefs.edit().putString("profile", json.toString()).putString("name", profile.name).apply()
    }

    fun clear() {
        prefs.edit().remove("session").remove("user").remove("name").remove("profile").apply()
    }
}

fun playbackPreferences(context: Context): SharedPreferences {
    val id = AccountSession(context).userId
    return context.getSharedPreferences(
        if (id == null) "viewing" else "viewing-account-$id",
        Context.MODE_PRIVATE,
    )
}

class AccountRepository(private val context: Context, val api: AccountApi = AccountApi()) {
    val session = AccountSession(context)
    val local = LocalLibrary(context)
    val signedIn: Boolean
        get() = session.userId != null

    private fun cachedLists(): AccountLists? =
        runCatching {
                val id = session.userId ?: return null
                val prefs = context.getSharedPreferences("account-lists-$id", Context.MODE_PRIVATE)
                val f = prefs.getString("favorites", null) ?: return null
                AccountLists(
                    parseFavoriteIds(f),
                    parseCollectionIds(prefs.getString("collections", "[]")!!),
                )
            }
            .getOrNull()

    var lists: AccountLists? = if (signedIn) cachedLists() else local.lists()
        private set

    private fun saveLists() {
        val id = session.userId ?: return
        val value = lists ?: return
        val favorites = JSONArray()
        value.favorites.forEach { favorites.put(it) }
        val collections = JSONArray()
        value.collections.forEach { (id, type) ->
            collections.put(JSONArray().put(id).put(type.name))
        }
        context
            .getSharedPreferences("account-lists-$id", Context.MODE_PRIVATE)
            .edit()
            .putString("favorites", favorites.toString())
            .putString("collections", collections.toString())
            .apply()
    }

    private val queueMutex = Mutex()
    private var remote: Map<String, AccountTimecode> = emptyMap()
    private val appliedProgress = mutableSetOf<Int>()

    private fun token() = session.token() ?: error("Войдите в аккаунт AniLiberty")

    suspend fun connect(value: String) {
        val profile = api.profile(value)
        val loaded =
            try {
                api.lists(value)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        session.save(value, profile)
        lists = loaded ?: cachedLists()
        saveLists()
        remote = emptyMap()
        appliedProgress.clear()
    }

    suspend fun refresh() {
        val value = token()
        val profile = api.profile(value)
        val loaded = api.lists(value)
        if (session.token() == value) {
            session.saveProfile(profile)
            lists = loaded
            saveLists()
        }
    }

    suspend fun favorite(id: Int, added: Boolean) {
        if (!signedIn) {
            local.favorite(id, added)
            lists = local.lists()
            return
        }
        val value = token()
        api.favorite(value, id, added)
        if (session.token() != value) return
        lists =
            lists?.let { it.copy(favorites = if (added) it.favorites + id else it.favorites - id) }
        saveLists()
    }

    suspend fun collection(id: Int, type: CollectionType?) {
        if (!signedIn) {
            local.collection(id, type)
            lists = local.lists()
            return
        }
        val value = token()
        api.collection(value, id, type)
        if (session.token() != value) return
        lists =
            lists?.let {
                it.copy(
                    collections =
                        if (type == null) it.collections - id else it.collections + (id to type)
                )
            }
        saveLists()
    }

    suspend fun loadProgress() {
        val value = token()
        flush()
        val loaded = api.timecodes(value)
        if (session.token() == value) {
            remote = loaded.associateBy { it.episodeId }
            appliedProgress.clear()
        }
    }

    fun applyProgress(release: Release) {
        if (!signedIn || remote.isEmpty() || !appliedProgress.add(release.id)) return
        val prefs = playbackPreferences(context)
        val dirty = pending(prefs).keys
        val edit = prefs.edit()
        release.episodes.forEach { e ->
            val item = remote[e.id] ?: return@forEach
            if (e.id in dirty) return@forEach
            val duration = (e.duration * 1000).coerceAtLeast(if (item.watched) 1 else 0)
            val position =
                if (item.watched) maxOf(duration, (item.seconds * 1000).toLong(), 1)
                else (item.seconds * 1000).toLong()
            edit
                .putLong("position:${e.id}", position)
                .putLong("duration:${e.id}", if (item.watched) position else duration)
        }
        if (prefs.getString("last:${release.id}", null) == null) {
            release.episodes
                .lastOrNull { e -> remote[e.id]?.let { it.watched || it.seconds > 0 } == true }
                ?.let { edit.putString("last:${release.id}", it.id) }
        }
        edit.apply()
    }

    suspend fun flush() =
        queueMutex.withLock {
            val value = token()
            val prefs = playbackPreferences(context)
            val queued = pending(prefs)
            for (batch in queued.values.chunked(100)) {
                api.updateTimecodes(value, batch)
                if (session.token() != value) return@withLock
                // A player may have saved a newer position while this request was running.
                synchronized(progressLock) {
                    val current = pending(prefs).toMutableMap()
                    batch.forEach { if (current[it.episodeId] == it) current.remove(it.episodeId) }
                    writePending(prefs, current.values.toList())
                }
            }
        }

    suspend fun releases(screen: Screen, page: Int): CatalogPage =
        if (signedIn) api.releases(token(), screen, page)
        else local.releases(screen).let { CatalogPage(it, false, it.size) }

    suspend fun references(favorites: Boolean): Map<String, List<CatalogChoice>> =
        if (signedIn) api.references(token(), favorites) else local.references(favorites)

    suspend fun toggleAdPreference(key: String) {
        val value = token()
        val profile = api.toggleAdPreference(value, key)
        if (session.token() == value) session.saveProfile(profile)
    }

    suspend fun updateIdentity(nickname: Boolean, value: String) {
        val token = token()
        val profile = api.updateIdentity(token, nickname, value)
        if (session.token() == token) session.saveProfile(profile)
    }

    suspend fun sessions(): List<AccountDeviceSession> = api.sessions(token())

    suspend fun deleteSession(id: String) {
        api.deleteSession(token(), id)
    }

    suspend fun deleteOtherSessions() {
        api.deleteOtherSessions(token())
    }

    suspend fun history(page: Int = 1): AccountHistoryPage =
        if (signedIn) api.history(token(), page) else local.history()

    suspend fun recentHistory(): List<AccountHistoryItem> = api.recentHistory(token())

    suspend fun localImportPlan(): List<AccountTimecode> {
        loadProgress()
        return importCandidates(
            localTimecodes(context.getSharedPreferences("viewing", Context.MODE_PRIVATE)),
            remote.keys,
        )
    }

    suspend fun importLocalHistory(): Int {
        val value = token()
        val loaded = api.lists(value)
        val localLists = local.lists()
        val plan = localImportPlan()
        fun checkSession() {
            check(session.token() == value) { "Аккаунт изменился. Повторите перенос." }
        }
        for (id in localLists.favorites - loaded.favorites) {
            checkSession()
            api.favorite(value, id, true)
        }
        for ((id, type) in localLists.collections.filterKeys { it !in loaded.collections }) {
            checkSession()
            api.collection(value, id, type)
        }
        for (batch in plan.chunked(100)) {
            checkSession()
            api.updateTimecodes(value, batch)
        }
        checkSession()
        refresh()
        loadProgress()
        return plan.size
    }

    suspend fun logout(): Boolean {
        val value = session.token()
        return try {
            if (value != null) api.logout(value)
            true
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        } finally {
            session.clear()
            lists = local.lists()
            remote = emptyMap()
            appliedProgress.clear()
        }
    }
}

private val progressLock = Any()

internal fun pending(prefs: SharedPreferences): Map<String, AccountTimecode> =
    runCatching {
            parseTimecodes(prefs.getString("account-pending", "[]")!!).associateBy { it.episodeId }
        }
        .getOrDefault(emptyMap())

private fun writePending(prefs: SharedPreferences, values: List<AccountTimecode>) {
    val json = JSONArray()
    values.forEach { json.put(JSONArray().put(it.episodeId).put(it.seconds).put(it.watched)) }
    prefs.edit().putString("account-pending", json.toString()).apply()
}

fun queueAccountProgress(context: Context, episodeId: String, position: Long, duration: Long) {
    queueAccountTimecodes(
        context,
        listOf(
            AccountTimecode(
                episodeId,
                position.coerceAtLeast(0) / 1000.0,
                isEpisodeWatched(position, duration, TvSettings(context).watchedPercent),
            )
        ),
    )
}

fun queueAccountTimecodes(context: Context, updates: List<AccountTimecode>) {
    if (AccountSession(context).userId == null) return
    val prefs = playbackPreferences(context)
    synchronized(progressLock) {
        val values = pending(prefs).toMutableMap()
        updates.forEach { values[it.episodeId] = it }
        writePending(prefs, values.values.toList())
    }
}

/** Import only explicitly started guest episodes; remote records always take precedence. */
internal fun localTimecodes(prefs: SharedPreferences): List<AccountTimecode> =
    prefs.all.keys
        .filter { it.startsWith("position:") }
        .mapNotNull { key ->
            val id = key.removePrefix("position:")
            val position = prefs.getLong(key, 0)
            val duration = prefs.getLong("duration:$id", 0)
            if (position <= 0 || id.isBlank()) null
            else AccountTimecode(id, position / 1000.0, isEpisodeWatched(position, duration, watchedPercent(prefs)))
        }

internal fun importCandidates(
    local: List<AccountTimecode>,
    remoteIds: Set<String>,
): List<AccountTimecode> = local.filter { it.episodeId !in remoteIds }
