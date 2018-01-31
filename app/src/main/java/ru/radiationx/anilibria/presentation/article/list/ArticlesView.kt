package ru.radiationx.anilibria.presentation.article.list

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.article.ArticleItem
import ru.radiationx.anilibria.entity.app.vital.VitalItem
import ru.radiationx.anilibria.utils.mvp.IBaseView

/**
 * Created by radiationx on 18.12.17.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface ArticlesView : IBaseView {
    @StateStrategyType(AddToEndStrategy::class)
    fun showArticles(articles: List<ArticleItem>)

    @StateStrategyType(AddToEndStrategy::class)
    fun insertMore(articles: List<ArticleItem>)

    fun setEndless(enable: Boolean)

    fun showVitalItems(vital: List<VitalItem>)
}
