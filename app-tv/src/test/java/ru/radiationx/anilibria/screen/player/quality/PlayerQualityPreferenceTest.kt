package ru.radiationx.anilibria.screen.player.quality

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.radiationx.data.entity.common.PlayerQuality
import ru.radiationx.data.entity.domain.release.QualityInfo
import ru.radiationx.data.entity.domain.types.ReleaseId
import java.lang.reflect.Proxy

class PlayerQualityPreferenceTest {
    private val allQualities = QualityInfo("sd", "hd", "fhd")

    @Test
    fun `select highest quality within both screen dimensions`() {
        val cases = listOf(
            Triple(3840, 2160, PlayerQuality.FULLHD),
            Triple(1920, 1080, PlayerQuality.FULLHD),
            Triple(1366, 768, PlayerQuality.HD),
            Triple(1280, 720, PlayerQuality.HD),
            Triple(1920, 720, PlayerQuality.HD),
            Triple(1024, 768, PlayerQuality.SD),
            Triple(854, 480, PlayerQuality.SD),
            Triple(0, 0, PlayerQuality.SD),
        )
        cases.forEach { (width, height, expected) ->
            assertEquals("$width x $height", expected, screenPlayerQuality(width, height))
            assertEquals("$height x $width", expected, screenPlayerQuality(height, width))
        }
    }

    @Test
    fun `auto selects highest available episode quality below screen limit`() {
        assertEquals(PlayerQuality.FULLHD, resolvePlayerQuality(null, allQualities, PlayerQuality.FULLHD))
        assertEquals(PlayerQuality.HD, resolvePlayerQuality(null, QualityInfo("sd", "hd", null), PlayerQuality.FULLHD))
        assertEquals(PlayerQuality.HD, resolvePlayerQuality(null, allQualities, PlayerQuality.HD))
        assertEquals(PlayerQuality.SD, resolvePlayerQuality(null, QualityInfo("sd", null, "fhd"), PlayerQuality.HD))
        assertNull(resolvePlayerQuality(null, QualityInfo(null, null, null), PlayerQuality.FULLHD))
    }

    @Test
    fun `manual quality is used when present and unavailable manual quality uses auto`() {
        assertEquals(PlayerQuality.SD, resolvePlayerQuality(PlayerQuality.SD, allQualities, PlayerQuality.FULLHD))
        assertEquals(PlayerQuality.FULLHD, resolvePlayerQuality(PlayerQuality.HD, QualityInfo("sd", null, "fhd"), PlayerQuality.FULLHD))
        assertEquals(PlayerQuality.HD, resolvePlayerQuality(PlayerQuality.FULLHD, QualityInfo("sd", "hd", null), PlayerQuality.FULLHD))
    }

    @Test
    fun `all releases default to auto despite legacy global quality`() {
        val fixture = PreferenceFixture()
        fixture.stored["player_quality"] = "sd"
        val preferences = ReleaseQualityPreferences(fixture.preferences)
        assertNull(preferences.forRelease(ReleaseId(1)).value)
        assertNull(preferences.forRelease(ReleaseId(2)).value)
    }

    @Test
    fun `manual choice persists for one release and auto removes only that choice`() {
        val fixture = PreferenceFixture()
        val preferences = ReleaseQualityPreferences(fixture.preferences)
        preferences.forRelease(ReleaseId(1)).value = PlayerQuality.HD
        assertNull(preferences.forRelease(ReleaseId(2)).value)
        preferences.forRelease(ReleaseId(2)).value = PlayerQuality.SD

        val reopened = ReleaseQualityPreferences(fixture.preferences)
        assertEquals(PlayerQuality.HD, reopened.forRelease(ReleaseId(1)).value)
        assertEquals(PlayerQuality.SD, reopened.forRelease(ReleaseId(2)).value)
        reopened.forRelease(ReleaseId(1)).value = null
        assertNull(preferences.forRelease(ReleaseId(1)).value)
        assertEquals(PlayerQuality.SD, preferences.forRelease(ReleaseId(2)).value)
    }

    @Test
    fun `automatic fallback for one episode preserves the releases manual choice`() {
        val fixture = PreferenceFixture()
        val preferences = ReleaseQualityPreferences(fixture.preferences)
        val selection = preferences.forRelease(ReleaseId(1))
        selection.value = PlayerQuality.HD
        assertEquals(PlayerQuality.FULLHD, resolvePlayerQuality(selection.value, QualityInfo("sd", null, "fhd"), PlayerQuality.FULLHD))
        assertEquals(PlayerQuality.HD, selection.value)
        assertEquals(PlayerQuality.HD, resolvePlayerQuality(selection.value, allQualities, PlayerQuality.FULLHD))
    }

    @Test(timeout = 5000)
    fun `switching between auto and manual SD notifies player observers`() = runBlocking {
        val fixture = PreferenceFixture()
        val selection = ReleaseQualityPreferences(fixture.preferences).forRelease(ReleaseId(1))
        val values = mutableListOf<PlayerQuality?>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            selection.take(3).toList(values)
        }
        selection.value = PlayerQuality.SD
        yield()
        selection.value = null
        job.join()
        assertEquals(listOf(null, PlayerQuality.SD, null), values)
    }

    private class PreferenceFixture {
        val stored = mutableMapOf<String, String>()
        private val listeners = mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()
        val preferences: SharedPreferences = proxy(SharedPreferences::class.java) { name, args ->
            when (name) {
                "getString" -> stored[args!![0]] ?: args[1]
                "registerOnSharedPreferenceChangeListener" -> {
                    listeners.add(args!![0] as SharedPreferences.OnSharedPreferenceChangeListener)
                    null
                }
                "edit" -> editor()
                else -> error("Unexpected SharedPreferences call: $name")
            }
        }

        private fun editor(): SharedPreferences.Editor {
            val changes = mutableMapOf<String, String?>()
            lateinit var editor: SharedPreferences.Editor
            editor = proxy(SharedPreferences.Editor::class.java) { name, args ->
                when (name) {
                    "putString", "remove" -> {
                        changes[args!![0] as String] = if (name == "remove") null else args[1] as String?
                        editor
                    }
                    "apply" -> {
                        changes.forEach { (key, value) ->
                            if (value == null) stored.remove(key) else stored[key] = value
                            listeners.forEach { it.onSharedPreferenceChanged(preferences, key) }
                        }
                        null
                    }
                    else -> error("Unexpected Editor call: $name")
                }
            }
            return editor
        }

        private fun <T> proxy(type: Class<T>, handler: (String, Array<out Any?>?) -> Any?): T =
            requireNotNull(type.cast(Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, method, args ->
                handler(method.name, args)
            }))
    }
}
