package tv.aniliberty.next

/** Domain identifiers are v1 identifiers. Episode UUIDs are never derived from positions. */
data class Episode(
    val id: String,
    val ordinal: Double,
    val name: String,
    val preview: String?,
    val duration: Long,
    val streams: List<Pair<String, String>>,
    val order: Double,
    val updatedAt: String = "",
    val opening: EpisodeSkip? = null,
    val ending: EpisodeSkip? = null,
) {
    val label: String
        get() = name.ifBlank { "Эпизод ${number(ordinal)}" }
}

data class Credit(val role: String, val name: String)

data class Release(
    val id: Int,
    val alias: String,
    val title: String,
    val english: String,
    val poster: String?,
    val year: String,
    val season: String,
    val type: String,
    val age: String,
    val genres: List<String>,
    val description: String,
    val episodes: List<Episode>,
    val credits: List<Credit>,
    val blocked: Boolean,
    val latestEpisode: Episode? = null,
    val externalIds: ExternalIds = ExternalIds(),
    val ratings: List<Rating> = emptyList(),
    val ongoing: Boolean? = null,
    val updatedAt: String = "",
    val totalEpisodes: Int? = null,
    val releasedEpisodes: Int? = null,
    val favoriteCount: Long? = null,
    val publishDay: Int? = null,
    val scheduledEpisode: Double? = null,
    val fullSeason: Boolean = false,
) {
    val catalogueEpisodeCount: Int?
        get() = releasedEpisodes

    val metadata: String
        get() = listOf(year, season, type, age).filter(String::isNotBlank).joinToString(" · ")
}
