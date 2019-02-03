package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.Comment
import ru.radiationx.anilibria.model.data.remote.api.ArticleApi
import ru.radiationx.anilibria.model.data.remote.api.CommentApi
import ru.radiationx.anilibria.model.system.SchedulersProvider

class CommentsRepository(
        private val schedulers: SchedulersProvider,
        private val commentApi: CommentApi
) {

    fun getCommentsRelease(id: Int, page: Int): Observable<Paginated<List<Comment>>> = commentApi
            .getComments(id, page)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendCommentRelease(url: String, id: Int, text: String, sessId: String): Observable<Paginated<List<Comment>>> = commentApi
            .sendComment(url, id, text, sessId)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun getCommentsArticle(id: Int, page: Int): Observable<Paginated<List<Comment>>> = commentApi
            .getCommentsArticle(id, page)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun sendCommentArticle(url: String, id: Int, text: String, sessId: String): Observable<Paginated<List<Comment>>> = commentApi
            .sendCommentArticle(url, id, text, sessId)
            .toObservable()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}