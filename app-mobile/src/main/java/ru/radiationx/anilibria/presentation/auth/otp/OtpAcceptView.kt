package ru.radiationx.anilibria.presentation.auth.otp

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.radiationx.anilibria.presentation.common.IBaseView

@StateStrategyType(SkipStrategy::class)
interface OtpAcceptView : IBaseView {

    fun close()
    fun setState(success: Boolean, progress: Boolean, error: String?)
}