package tv.aniliberty.next

import android.app.Application
import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], application = Application::class)
class WatchedPercentTest {
    private val context = RuntimeEnvironment.getApplication()

    @Before
    fun clear() {
        context.getSharedPreferences("account-session", Context.MODE_PRIVATE).edit().clear().commit()
        playbackPreferences(context).edit().clear().commit()
    }

    @Test
    fun exactThresholdIsRequiredAndUnknownDurationNeverCompletes() {
        for (percent in listOf(50, 85, 90, 95, 100)) {
            val boundary = 1200000L * percent / 100
            assertFalse(isEpisodeWatched(boundary - 1, 1200000, percent))
            assertTrue(isEpisodeWatched(boundary, 1200000, percent))
        }
        assertFalse(isEpisodeWatched(0, 1200000))
        assertFalse(isEpisodeWatched(1200000, 0))
        assertFalse(isEpisodeWatched(1200000, -1))
    }

    @Test
    fun preferenceSurvivesRecreationAndAccountExportUsesIt() {
        val settings = TvSettings(context)
        assertEquals(90, settings.watchedPercent)
        settings.watchedPercent = 85
        assertEquals(85, TvSettings(context).watchedPercent)
        val prefs = playbackPreferences(context)
        prefs.edit().putLong("position:episode", 1020000)
            .putLong("duration:episode", 1200000).commit()
        assertTrue(localTimecodes(prefs).single().watched)
        settings.watchedPercent = 95
        assertFalse(localTimecodes(prefs).single().watched)
    }
}
