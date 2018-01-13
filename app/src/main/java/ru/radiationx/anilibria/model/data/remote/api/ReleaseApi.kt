package ru.radiationx.anilibria.model.data.remote.api;

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.GenreItem
import ru.radiationx.anilibria.entity.app.release.ReleaseFull
import ru.radiationx.anilibria.entity.app.release.ReleaseItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser

/* Created by radiationx on 31.10.17. */

class ReleaseApi(private val client: IClient,
                 apiUtils: IApiUtils) {

    private val releaseParser = ReleaseParser(apiUtils)

    fun getRelease(releaseId: Int): Single<ReleaseFull> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "release",
                "ELEMENT_ID" to releaseId.toString()
        )
        return client.get(Api.API_URL, args)
                .map { releaseParser.release(it) }
    }

    fun getRelease(releaseIdName: String): Single<ReleaseFull> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "release",
                "ELEMENT_CODE" to releaseIdName
        )
        return client.get(Api.API_URL, args)
                .map { releaseParser.release(it) }
    }

    fun getGenres(): Single<List<GenreItem>> {
        val args: MutableMap<String, String> = mutableMapOf("action" to "tags")
        return client.get(Api.API_URL, args)
                .map { releaseParser.genres(it) }
    }

    fun getReleases(page: Int): Single<Paginated<List<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf("PAGEN_1" to page.toString())
        return client.get(Api.API_URL, args)
                .map { releaseParser.releases(it) }
    }

    fun getFavorites(page: Int): Single<Paginated<List<ReleaseItem>>> {
        //val args: MutableMap<String, String> = mutableMapOf("PAGEN_1" to page.toString())
        val args: MutableMap<String, String> = mutableMapOf("SHOWALL_1" to "1")
        return client.get("${Api.BASE_URL}izbrannoe.php", args)
                .map { releaseParser.favorites(it) }
    }

}
