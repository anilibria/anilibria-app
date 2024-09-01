package ru.radiationx.anilibria.common

import android.content.Context
import ru.radiationx.data.entity.domain.feed.FeedItem
import ru.radiationx.data.entity.domain.release.Release
import ru.radiationx.data.entity.domain.youtube.YoutubeItem
import ru.radiationx.shared.ktx.android.relativeDate
import ru.radiationx.shared.ktx.capitalizeDefault
import ru.radiationx.shared.ktx.decapitalizeDefault

class CardsDataConverter(
    private val context: Context,
) {

    fun toCard(releaseItem: Release) = releaseItem.run {
        val seasonText = "$year ${season.orEmpty()}"
        val genreText = genres.firstOrNull()?.capitalizeDefault()
        val updateText = "Обновлен ${updatedAt.relativeDate(context).decapitalizeDefault()}"
        val descItems = listOfNotNull(seasonText, genreText, updateText)
        LibriaCard(
            names.main,
            descItems.joinToString(" • "),
            poster.orEmpty(),
            LibriaCard.Type.Release(releaseItem.id)
        )
    }

    fun toCard(youtubeItem: YoutubeItem) = youtubeItem.run {
        LibriaCard(
            title.orEmpty(),
            "Вышел ${createdAt.relativeDate(context).decapitalizeDefault()}",
            image.orEmpty(),
            LibriaCard.Type.Youtube(youtubeItem.link)
        )
    }

    fun toCard(feedItem: FeedItem): LibriaCard = feedItem.run {
        when {
            release != null -> toCard(release!!)
            youtube != null -> toCard(youtube!!)
            else -> throw RuntimeException("WataFuq")
        }
    }
}