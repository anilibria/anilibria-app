package tv.aniliberty.next

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun updateTimestamp(value: String, zone: ZoneId = ZoneId.systemDefault()): String {
    val instant =
        runCatching { Instant.parse(value) }
            .getOrElse { runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull() }
            ?: return ""
    return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(zone).format(instant)
}

/** Time elapsed since publication; older updates use the TV's local date. */
fun relativeUpdateLabel(
    value: String,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val instant = runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull() ?: return ""
    val minutes = java.time.Duration.between(instant, now).toMinutes().coerceAtLeast(0)
    fun ago(count: Long, one: String, few: String, many: String): String {
        val noun =
            when {
                count % 100 in 11..14 -> many
                count % 10 == 1L -> one
                count % 10 in 2..4 -> few
                else -> many
            }
        return "Обновлён $count $noun назад"
    }
    return when {
        minutes < 1 -> "Обновлён только что"
        minutes < 60 -> ago(minutes, "минуту", "минуты", "минут")
        minutes < 1440 -> ago(minutes / 60, "час", "часа", "часов")
        minutes < 10080 -> ago(minutes / 1440, "день", "дня", "дней")
        else ->
            "Обновлён " + DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(zone).format(instant)
    }
}
