package ru.radiationx.anilibria.presentation.comments

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.data.entity.app.page.VkComments
import ru.radiationx.anilibria.presentation.common.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface VkCommentsView : IBaseView {

    fun showBody(comments: VkComments)
}