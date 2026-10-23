package tv.aniliberty.next

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class VideoTest {
    @Test
    fun extraInformationContainsOnlyChannelAndDate() {
        val item =
            parseHomeVideos(
                    """[{"id":1,"title":"Ролик","views":999,"comments":10,"created_at":"2026-10-21T12:00:00Z","origin":{"title":"Канал"}}]"""
                )
                .single()
        assertEquals("Канал · 21.10.2026", item.subtitle)
    }

    @Test
    fun parsesNativeArrayWithPreviewAndSourceLink() {
        val item =
            parseHomeVideos(
                    """[{"id":1,"title":"Ролик","url":"https://www.youtube.com/watch?v=abcdefghijk","image":{"optimized":{"preview":"/video.webp"}},"origin":{"title":"AniLiberty"}}]"""
                )
                .single()
        assertEquals("Ролик", item.title)
        assertEquals("https://cdn.anilibria.top/video.webp", item.image)
        assertTrue(item.url.contains("abcdefghijk"))
        assertEquals("AniLiberty", item.subtitle)
    }

    @Test
    fun derivesYoutubeLinkOnlyForRecognizedOriginAndId() {
        val item =
            parseHomeVideos(
                    """[{"id":2,"video_id":"abcdefghijk","origin":{"type":{"value":"YOUTUBE_PLAYLIST"}}}]"""
                )
                .single()
        assertEquals("https://www.youtube.com/watch?v=abcdefghijk", item.url)
        assertEquals("", parseHomeVideos("""[{"id":3,"video_id":"bad"}]""").single().url)
        assertTrue(parseHomeVideos("[]").isEmpty())
    }
}
