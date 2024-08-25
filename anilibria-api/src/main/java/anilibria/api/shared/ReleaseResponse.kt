package anilibria.api.shared


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseResponse(
    @Json(name = "id")
    val id: Int,
    @Json(name = "type")
    val type: Type,
    @Json(name = "year")
    val year: Int,
    @Json(name = "name")
    val name: Name,
    @Json(name = "alias")
    val alias: String,
    @Json(name = "season")
    val season: Season,
    @Json(name = "poster")
    val poster: Poster,
    @Json(name = "fresh_at")
    val freshAt: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String,
    @Json(name = "is_ongoing")
    val isOngoing: Boolean,
    @Json(name = "age_rating")
    val ageRating: AgeRating,
    @Json(name = "publish_day")
    val publishDay: PublishDay,
    @Json(name = "description")
    val description: String,
    @Json(name = "notification")
    val notification: String,
    @Json(name = "episodes_total")
    val episodesTotal: Int,
    @Json(name = "external_player")
    val externalPlayer: String,
    @Json(name = "is_in_production")
    val isInProduction: Boolean,
    @Json(name = "is_blocked_by_geo")
    val isBlockedByGeo: Boolean,
    @Json(name = "episodes_are_unknown")
    val episodesAreUnknown: Boolean,
    @Json(name = "is_blocked_by_copyrights")
    val isBlockedByCopyrights: Boolean,
    @Json(name = "added_in_users_favorites")
    val addedInUsersFavorites: Int,
    @Json(name = "average_duration_of_episode")
    val averageDurationOfEpisode: Int,
    @Json(name = "genres")
    val genres: List<Genre>,
    @Json(name = "episodes")
    val episodes: List<Episode>
) {
    @JsonClass(generateAdapter = true)
    data class Type(
        @Json(name = "value")
        val value: String,
        @Json(name = "description")
        val description: String
    )

    @JsonClass(generateAdapter = true)
    data class Name(
        @Json(name = "main")
        val main: String,
        @Json(name = "english")
        val english: String,
        @Json(name = "alternative")
        val alternative: String
    )

    @JsonClass(generateAdapter = true)
    data class Season(
        @Json(name = "value")
        val value: String,
        @Json(name = "description")
        val description: String
    )

    @JsonClass(generateAdapter = true)
    data class Poster(
        @Json(name = "src")
        val src: String,
        @Json(name = "thumbnail")
        val thumbnail: String,
        @Json(name = "optimized")
        val optimized: Optimized
    ) {
        @JsonClass(generateAdapter = true)
        data class Optimized(
            @Json(name = "src")
            val src: String,
            @Json(name = "thumbnail")
            val thumbnail: String
        )
    }

    @JsonClass(generateAdapter = true)
    data class AgeRating(
        @Json(name = "value")
        val value: String,
        @Json(name = "label")
        val label: String,
        @Json(name = "is_adult")
        val isAdult: Boolean,
        @Json(name = "description")
        val description: String
    )

    @JsonClass(generateAdapter = true)
    data class PublishDay(
        @Json(name = "value")
        val value: String,
        @Json(name = "description")
        val description: String
    )

    @JsonClass(generateAdapter = true)
    data class Genre(
        @Json(name = "id")
        val id: Int,
        @Json(name = "name")
        val name: String,
        @Json(name = "total_releases")
        val totalReleases: Int,
        @Json(name = "image")
        val image: Image
    ) {
        @JsonClass(generateAdapter = true)
        data class Image(
            @Json(name = "preview")
            val preview: String,
            @Json(name = "thumbnail")
            val thumbnail: String,
            @Json(name = "optmized")
            val optmized: Optmized
        ) {
            @JsonClass(generateAdapter = true)
            data class Optmized(
                @Json(name = "preview")
                val preview: String,
                @Json(name = "thumbnail")
                val thumbnail: String
            )
        }
    }

    @JsonClass(generateAdapter = true)
    data class Episode(
        @Json(name = "id")
        val id: String,
        @Json(name = "name")
        val name: String,
        @Json(name = "ordinal")
        val ordinal: Double,
        @Json(name = "opening")
        val opening: Opening,
        @Json(name = "ending")
        val ending: Ending,
        @Json(name = "preview")
        val preview: Preview,
        @Json(name = "hls_480")
        val hls480: String,
        @Json(name = "hls_720")
        val hls720: String,
        @Json(name = "hls_1080")
        val hls1080: String,
        @Json(name = "duration")
        val duration: Int,
        @Json(name = "rutube_id")
        val rutubeId: String,
        @Json(name = "youtube_id")
        val youtubeId: String,
        @Json(name = "updated_at")
        val updatedAt: String,
        @Json(name = "sort_order")
        val sortOrder: Int,
        @Json(name = "name_english")
        val nameEnglish: String
    ) {
        @JsonClass(generateAdapter = true)
        data class Opening(
            @Json(name = "start")
            val start: Int,
            @Json(name = "stop")
            val stop: Int
        )

        @JsonClass(generateAdapter = true)
        data class Ending(
            @Json(name = "start")
            val start: Int,
            @Json(name = "stop")
            val stop: Int
        )

        @JsonClass(generateAdapter = true)
        data class Preview(
            @Json(name = "src")
            val src: String,
            @Json(name = "thumbnail")
            val thumbnail: String,
            @Json(name = "optmized")
            val optmized: Optmized
        ) {
            @JsonClass(generateAdapter = true)
            data class Optmized(
                @Json(name = "src")
                val src: String,
                @Json(name = "thumbnail")
                val thumbnail: String
            )
        }
    }
}