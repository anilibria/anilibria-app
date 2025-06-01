package ru.radiationx.anilibria.common

import ru.radiationx.data.api.releases.models.PlayerQuality
import ru.radiationx.data.api.releases.models.Release
import ru.radiationx.data.api.schedule.models.PublishDay
import ru.radiationx.data.api.schedule.models.asDayNameDeclension
import ru.radiationx.data.api.schedule.models.asDayPretext
import ru.radiationx.data.app.episodeaccess.models.EpisodeAccess
import java.text.NumberFormat
import javax.inject.Inject

class DetailDataConverter @Inject constructor() {

    fun toDetail(
        releaseItem: Release,
        isFull: Boolean,
        accesses: List<EpisodeAccess>,
        isInFavorites: Boolean
    ): LibriaDetails = releaseItem.run {
        val types = listOfNotNull(
            type,
            averageEpisodeDuration?.let { "~$it мин" },
            ageRating
        )
        LibriaDetails(
            id = id,
            titleRu = names.main,
            titleEn = names.english,
            extra = listOfNotNull(
                genres.firstOrNull()?.name,
                "$year ${season.orEmpty()}",
                types.joinToString(),
                "Серий: ${episodes.ifEmpty { null }?.size ?: "Не доступно"}"
            ).joinToString(" • "),
            description = description.orEmpty(),
            announce = getAnnounce(isFull),
            image = poster,
            favoriteCount = NumberFormat.getNumberInstance().format(counters.favorites),
            hasFullHd = episodes.any { PlayerQuality.FULLHD in it.qualityInfo },
            isFavorite = isInFavorites,
            hasEpisodes = episodes.isNotEmpty(),
            hasViewed = accesses.any { it.isViewed },
            hasWebPlayer = webPlayer != null
        )
    }

    private fun Release.getAnnounce(isFull: Boolean): String {
        if (!isFull) return ""
        val status = if (isInProduction) publishDay.toAnnouncePublishDay() else "Релиз завершен"
        val episodesWarning = if (episodes.isEmpty()) {
            "Нет доступных для просмотра серий"
        } else {
            null
        }
        return listOfNotNull(status, announce, episodesWarning).joinToString(" • ")
    }

    private fun PublishDay.toAnnouncePublishDay(): String {
        return "Серии выходят ${this.asDayPretext()}, ${this.asDayNameDeclension()}"
    }
}