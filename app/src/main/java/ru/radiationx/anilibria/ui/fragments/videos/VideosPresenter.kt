package ru.radiationx.anilibria.ui.fragments.videos

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.repository.ArticleRepository
import ru.radiationx.anilibria.ui.fragments.articles.ArticlesPresenter
import ru.terrakok.cicerone.Router

/**
 * Created by mintrocket on 20.12.2017.
 */
@InjectViewState
class VideosPresenter(private val articleRepository: ArticleRepository,
                      private val router: Router) : ArticlesPresenter(articleRepository, router) {
    override var category: String = Api.CATEGORY_VIDEOS
}
