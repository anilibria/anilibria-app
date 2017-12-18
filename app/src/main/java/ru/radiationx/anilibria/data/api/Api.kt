package ru.radiationx.anilibria.data.api

import ru.radiationx.anilibria.data.api.modules.Articles
import ru.radiationx.anilibria.data.api.modules.Releases
import ru.radiationx.anilibria.data.client.IClient

/* Created by radiationx on 31.10.17. */

class Api(client: IClient) {
    companion object {
        val BASE_URL = "http://www.anilibria.tv/"
        val API_URL = BASE_URL + "api/api.php"
        val ARTICLES_URL = BASE_URL + "novosti/"
    }

    private var releases: Releases = Releases(client)
    private var articles: Articles = Articles(client)


    /* Releases */
    fun getRelease(releaseId: Int) = releases.getRelease(releaseId)

    fun getGenres() = releases.getGenres()

    fun searchRelease(name: String, genre: String, page: Int) = releases.searchReleases(name, genre, page)

    fun getReleases(page: Int) = releases.getReleases(page)


    /* Articles */

    fun getArticle(articleId: String) = articles.getArticle(articleId)

    fun getArticles(page: Int) = articles.getArticles(page)

}
