package ru.radiationx.anilibria.presentation.article.details

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.model.repository.ArticleRepository
import ru.radiationx.anilibria.utils.mvp.BasePresenter
import ru.terrakok.cicerone.Router

/**
 * Created by radiationx on 20.12.17.
 */
@InjectViewState
class ArticlePresenter(private val articleRepository: ArticleRepository,
                       private val router: Router) : BasePresenter<ArticleView>(router) {

    var url: String = ""

    fun setDataFromItem(item: ArticleItem) {
        item.run {
            viewState.preShow(imageUrl, title, userNick, commentsCount, viewsCount)
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadArticle(url)
    }

    fun loadArticle(articleUrl: String) {
        Log.e("SUKA", "loadArticle")
        viewState.setRefreshing(true)
        val disposable = articleRepository.getArticle(articleUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ article ->
                    viewState.setRefreshing(false)
                    viewState.showArticle(article)
                }) { throwable ->
                    viewState.setRefreshing(false)
                    Log.d("SUKA", "SAS")
                    throwable.printStackTrace()
                }
        addDisposable(disposable)
    }
}
