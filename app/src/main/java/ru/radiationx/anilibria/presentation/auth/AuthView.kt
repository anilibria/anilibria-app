package ru.radiationx.anilibria.presentation.auth

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView

/**
 * Created by radiationx on 30.12.17.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface AuthView : IBaseView {
    fun setSignButtonEnabled(isEnabled: Boolean)
    @StateStrategyType(SkipStrategy::class)
    fun showRegistrationDialog()
}