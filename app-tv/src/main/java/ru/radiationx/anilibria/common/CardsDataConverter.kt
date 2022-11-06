package ru.radiationx.anilibria.common

import android.content.Context
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.shared.ktx.android.relativeDate
import java.util.*

class CardsDataConverter(
    private val context: Context
) {

    fun toCard(releaseItem: Release) = releaseItem.run {
        LibriaCard(
            title.orEmpty(),
            "${seasons.firstOrNull()} год • ${genres.firstOrNull()
                ?.capitalize()} • Серии: ${series?.trim() ?: "Не доступно"} • Обновлен ${Date(torrentUpdate * 1000L).relativeDate(context)
                .decapitalize()}",
            poster.orEmpty(),
            LibriaCard.Type.RELEASE
        ).apply {
            rawData = releaseItem
        }
    }

    fun toCard(youtubeItem: YoutubeItem) = youtubeItem.run {
        LibriaCard(
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
}