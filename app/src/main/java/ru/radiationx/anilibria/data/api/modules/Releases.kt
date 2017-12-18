package ru.radiationx.anilibria.data.api.modules;

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.mappers.ReleasesMapper
import ru.radiationx.anilibria.data.api.models.GenreItem
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.api.models.ReleaseItem
import ru.radiationx.anilibria.data.client.IClient

/* Created by radiationx on 31.10.17. */

class Releases(private val client: IClient) {

    fun getRelease(releaseId: Int): Single<ReleaseItem> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "release",
                "ELEMENT_ID" to releaseId.toString()
        )
        return client.get(Api.API_URL, args)
                .map { ReleasesMapper.release(it) }
    }

    fun getGenres(): Single<List<GenreItem>> {
        val args: MutableMap<String, String> = mutableMapOf("action" to "tags")
        return client.get(Api.API_URL, args)
                .map { ReleasesMapper.genres(it) }
    }

    fun searchReleases(name: String, genre: String, page: Int): Single<Paginated<ArrayList<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "search",
                "genre" to genre,
                "name" to name,
                "PAGEN_1" to page.toString()
        )
        return client.get(Api.API_URL, args)
                .map { ReleasesMapper.releases(it) }
    }

    fun getReleases(page: Int): Single<Paginated<ArrayList<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf("PAGEN_1" to page.toString())
        return client.get(Api.API_URL, args)
                .map { ReleasesMapper.releases(it) }
    }

}
