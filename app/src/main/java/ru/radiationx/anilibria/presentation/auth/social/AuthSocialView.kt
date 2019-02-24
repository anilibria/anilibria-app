package ru.radiationx.anilibria.presentation.auth.social

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView

interface AuthSocialView : IBaseView {
    fun loadPage(url: String)
    @StateStrategyType(SkipStrategy::class)
    fun showError()
}