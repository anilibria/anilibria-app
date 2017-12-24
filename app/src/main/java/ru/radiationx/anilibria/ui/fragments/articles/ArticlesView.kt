package ru.radiationx.anilibria.ui.fragments.articles

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.data.api.models.ArticleItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

/**
 * Created by radiationx on 18.12.17.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface ArticlesView : IBaseView {
    @StateStrategyType(AddToEndStrategy::class)
    fun showArticles(articles: List<ArticleItem>);

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMore(articles: List<ArticleItem>);

    fun setEndless(enable: Boolean)
}