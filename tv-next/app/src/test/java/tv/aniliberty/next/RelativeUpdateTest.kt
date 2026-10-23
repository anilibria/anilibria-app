package tv.aniliberty.next

import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class RelativeUpdateTest {
    @Test
    fun displaysDateAndTimeInTvTimeZone() {
        assertEquals(
            "22.10.2026 15:40",
            updateTimestamp("2026-10-22T12:40:00Z", ZoneId.of("Europe/Moscow")),
        )
        assertEquals(
            "22.10.2026 15:40",
            updateTimestamp("2026-10-22T15:40:00+03:00", ZoneId.of("Europe/Moscow")),
        )
        assertEquals(
            "23.10.2026 01:30",
            updateTimestamp("2026-10-22T22:30:00Z", ZoneId.of("Europe/Moscow")),
        )
    }

    @Test
    fun hidesMissingAndInvalidTimestamps() {
        assertEquals("", updateTimestamp(""))
        assertEquals("", updateTimestamp("invalid"))
    }

    @Test
    fun relativeLabelsFollowElapsedTimeBoundaries() {
        val now = java.time.Instant.parse("2026-10-22T12:00:00Z")
        val cases =
            mapOf(
                0L to "Обновлён только что",
                1L to "Обновлён 1 минуту назад",
                2L to "Обновлён 2 минуты назад",
                11L to "Обновлён 11 минут назад",
                21L to "Обновлён 21 минуту назад",
                59L to "Обновлён 59 минут назад",
                60L to "Обновлён 1 час назад",
                120L to "Обновлён 2 часа назад",
                660L to "Обновлён 11 часов назад",
                1439L to "Обновлён 23 часа назад",
                1440L to "Обновлён 1 день назад",
                2880L to "Обновлён 2 дня назад",
                8640L to "Обновлён 6 дней назад",
                10080L to "Обновлён 15.10.2026",
            )
        cases.forEach { (minutes, expected) ->
            assertEquals(
                expected,
                relativeUpdateLabel(
                    now.minusSeconds(minutes * 60).toString(),
                    now,
                    ZoneId.of("UTC"),
                ),
            )
        }
        assertEquals("", relativeUpdateLabel("invalid", now))
        assertEquals("", relativeUpdateLabel("", now))
        assertEquals(
            "Обновлён только что",
            relativeUpdateLabel(now.plusSeconds(60).toString(), now),
        )
        assertEquals(
            "Обновлён 16.10.2026",
            relativeUpdateLabel(
                "2026-10-15T23:30:00Z",
                now.plusSeconds(86400),
                ZoneId.of("Europe/Moscow"),
            ),
        )
    }
}
