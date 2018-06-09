package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.model.data.remote.api.ArticleApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

/**
 * Created by radiationx on 18.12.17.
 */
class ArticleRepository(
        private val schedulers: SchedulersProvider,
        private val articleApi: ArticleApi
) {

    /*
    * 1.    String  Video ID
    * */
    private val iframeYT = "<iframe[^>]*?src=\"(?:http(?:s?):)?\\/\\/(?:www\\.)?youtu(?:be\\.com\\/watch\\?v=|\\.be\\/|be.com\\/embed\\/)([\\w\\-\\_]*)(&(amp;)[\\w\\=]*)?[^\"]*?\"[^>]*?>[\\s\\S]*?<\\/iframe>"

    /*
    * 1.    String  oid
    * 2.    String  id
    * 3.    String? Какой-то хеш и всякое такое
    * */
    private val iframeVK = "<iframe[^>]*?src=\"(?:http(?:s?):)?\\/\\/(?:www\\.)?vk\\.com\\/video_ext\\.php\\?oid=([^&\"]*?)&id=([^&\"]*?)(&hash[^\"]*?)?\"[^>]*?>[\\s\\S]*?<\\/iframe>"

    /*
    * Изображение линии, нужно чтобы было обёрнуто в див, тогда вёрстка не полетит
    * */
    private val alibBordLine = "<img[^>]*?src=\"[^\"]*?borderline\\.[^\"]*?\"[^>]*?>"

    fun getArticle(code: String): Observable<ArticleItem> = articleApi
            .getArticle(code)
            .map {
                it.content = it.content.replace(Regex(iframeYT), "<div class=\"alib_button yt\"><a href=\"https://youtu.be/$1\">Смотреть на YouTube</a></div>")
                it.content = it.content.replace(Regex(iframeVK), "<div class=\"alib_button vk\"><a href=\"https://vk.com/video?z=video$1_$2$3\">Смотреть в VK</a></div>")
                it.content = it.content.replace(Regex(alibBordLine), "<div class=\"alib_borderline\">$0</div>")
                it
            }
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())


    fun getArticles(category: String, page: Int): Observable<Paginated<List<ArticleItem>>> = articleApi
            .getArticles(category, page)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getComments(id: Int, page: Int): Observable<Paginated<List<Comment>>> = articleApi
            .getComments(id, page)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendComment(url: String, id: Int, text: String, sessId: String): Observable<Paginated<List<Comment>>> = articleApi
            .sendComment(url, id, text, sessId)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
