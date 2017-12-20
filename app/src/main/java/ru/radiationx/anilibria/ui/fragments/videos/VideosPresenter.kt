package ru.radiationx.anilibria.ui.fragments.videos

import com.arellomobile.mvp.InjectViewState
import ru.radiationx.anilibria.data.api.Api
import ru.radiationx.anilibria.data.repository.ArticlesRepository
import ru.radiationx.anilibria.ui.fragments.articles.ArticlesPresenter
import ru.terrakok.cicerone.Router

/**
 * Created by mintrocket on 20.12.2017.
 */
@InjectViewState
class VideosPresenter(private val articlesRepository: ArticlesRepository,
                      private val router: Router) : ArticlesPresenter(articlesRepository, router) {
    override var categoryName: String = Api.CATEGORY_VIDEOS
}
