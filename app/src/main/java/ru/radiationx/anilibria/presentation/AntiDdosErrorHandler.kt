package ru.radiationx.anilibria.presentation

import android.os.Bundle
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.model.data.BlazingFastException
import ru.radiationx.anilibria.model.data.GoogleCaptchaException
import ru.radiationx.anilibria.model.data.remote.IAntiDdosErrorHandler
import ru.radiationx.anilibria.model.interactors.AntiDdosInteractor
import ru.terrakok.cicerone.Router

class AntiDdosErrorHandler(
        private val antiDdosInteractor: AntiDdosInteractor,
        private val router: Router
) : IAntiDdosErrorHandler {

    override fun handle(throwable: Throwable) {
        if (throwable is GoogleCaptchaException) {
            antiDdosInteractor.newExceptionEvent(AntiDdosInteractor.EVENT_GOOGLE_CAPTCHA)
            router.navigateTo(Screens.GOOGLE_CAPTCHA, Bundle().apply {
                putString("content", throwable.content)
                putString("url", throwable.url)
            })
        }
        if (throwable is BlazingFastException) {
            antiDdosInteractor.newExceptionEvent(AntiDdosInteractor.EVENT_BLAZING_FAST)
            router.navigateTo(Screens.BLAZINFAST, Bundle().apply {
                putString("content", throwable.content)
                putString("url", throwable.url)
            })
        }
    }

}