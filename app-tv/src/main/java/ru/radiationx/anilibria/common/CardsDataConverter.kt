package ru.radiationx.anilibria.common

import android.content.Context
import android.text.format.DateUtils
import ru.radiationx.data.entity.app.feed.FeedItem
import ru.radiationx.data.entity.app.release.ReleaseItem
import ru.radiationx.data.entity.app.youtube.YoutubeItem
import java.util.*

class CardsDataConverter(
    private val context: Context
) {

    fun toCard(releaseItem: ReleaseItem) = releaseItem.run {
        LibriaCard(
            id,
            title.orEmpty(),
            "${seasons.firstOrNull()} год • ${genres.firstOrNull()
                ?.capitalize()} • Серии: ${series} • Обновлен ${Date(torrentUpdate * 1000L).relativeDate(context).decapitalize()}",
            poster.orEmpty(),
            LibriaCard.Type.RELEASE
        ).apply {
            rawData = releaseItem
        }
    }

    fun toCard(youtubeItem: YoutubeItem) = youtubeItem.run {
        LibriaCard(
            id,
            title.orEmpty(),
            "Вышел ${Date(timestamp * 1000L).relativeDate(context).decapitalize()}",
            image.orEmpty(),
            LibriaCard.Type.YOUTUBE
        ).apply {
            rawData = youtubeItem
        }
    }

    fun toCard(feedItem: FeedItem): LibriaCard = feedItem.run {
        when {
            release != null -> toCard(release!!)
            youtube != null -> toCard(youtube!!)
            else -> throw RuntimeException("WataFuq")
        }
    }

    fun Date.relativeDate(context: Context) = DateUtils.getRelativeDateTimeString(
        context,
        time,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.DAY_IN_MILLIS * 2,
        DateUtils.FORMAT_SHOW_TIME
    ).toString()
}