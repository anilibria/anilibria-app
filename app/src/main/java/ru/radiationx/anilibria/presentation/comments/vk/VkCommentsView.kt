package ru.radiationx.anilibria.presentation.comments.vk

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.entity.app.page.VkComments
import ru.radiationx.anilibria.utils.mvp.IBaseView

@StateStrategyType(AddToEndSingleStrategy::class)
interface VkCommentsView : IBaseView {

    fun showBody(comments: VkComments)
}