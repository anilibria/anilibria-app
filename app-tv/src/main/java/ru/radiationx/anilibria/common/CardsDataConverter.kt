package ru.radiationx.anilibria.common

import android.content.Context
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.videos.models.YoutubeItem
import ru.radiationx.data.app.feed.models.FeedItem
import ru.radiationx.shared.ktx.android.relativeDate
import ru.radiationx.shared.ktx.capitalizeDefault
import ru.radiationx.shared.ktx.decapitalizeDefault
import javax.inject.Inject

class CardsDataConverter @Inject constructor(
    private val context: Context,
) {

    fun toCard(releaseItem: Release) = releaseItem.run {
        val seasonText = "$year ${season.orEmpty()}"
        val genreText = genres.firstOrNull()?.name?.capitalizeDefault()
        val updateText = "Обновлен ${freshAt.relativeDate(context).decapitalizeDefault()}"
        val descItems = listOfNotNull(seasonText, genreText, updateText)
        LibriaCard(
            title = names.main,
            description = descItems.joinToString(" • "),
            image = poster,
            type = LibriaCard.Type.Release(releaseItem.id)
        )
    }

    fun toCard(youtubeItem: YoutubeItem) = youtubeItem.run {
        LibriaCard(
            title = title.orEmpty(),
            description = "Вышел ${createdAt.relativeDate(context).decapitalizeDefault()}",
            image = image,
            type = LibriaCard.Type.Youtube(youtubeItem.link)
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