package ru.radiationx.anilibria.data.api.modules;

import io.reactivex.Single
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.api.models.Paginated
import ru.radiationx.anilibria.data.api.models.release.GenreItem
import ru.radiationx.anilibria.data.api.models.release.ReleaseFull
import ru.radiationx.anilibria.data.api.models.release.ReleaseItem
import ru.radiationx.anilibria.data.api.parsers.ReleaseParser
import ru.radiationx.anilibria.data.client.IClient

/* Created by radiationx on 31.10.17. */

class ReleaseApi(private val client: IClient) {

    fun getRelease(releaseId: Int): Single<ReleaseFull> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "release",
                "ELEMENT_ID" to releaseId.toString()
        )
        return client.get(Api.API_URL, args)
                .map { ReleaseParser.release(it) }
    }

    fun getGenres(): Single<List<GenreItem>> {
        val args: MutableMap<String, String> = mutableMapOf("action" to "tags")
        return client.get(Api.API_URL, args)
                .map { ReleaseParser.genres(it) }
    }

    fun getReleases(page: Int): Single<Paginated<List<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf("PAGEN_1" to page.toString())
        return client.get(Api.API_URL, args)
                .map { ReleaseParser.releases(it) }
    }

}
