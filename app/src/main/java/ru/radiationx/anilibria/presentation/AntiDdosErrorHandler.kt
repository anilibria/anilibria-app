package ru.radiationx.anilibria.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.model.data.BlazingFastException
import ru.radiationx.anilibria.model.data.GoogleCaptchaException
import ru.radiationx.anilibria.model.data.remote.IAntiDdosErrorHandler
import ru.radiationx.anilibria.model.interactors.AntiDdosInteractor
import ru.radiationx.anilibria.ui.activities.BlazingFastActivity
import ru.radiationx.anilibria.ui.activities.GoogleCaptchaActivity
import ru.terrakok.cicerone.Router

class AntiDdosErrorHandler(
        private val antiDdosInteractor: AntiDdosInteractor,
        private val context: Context
) : IAntiDdosErrorHandler {

    override fun handle(throwable: Throwable) {
        if (throwable is GoogleCaptchaException) {
            antiDdosInteractor.newExceptionEvent(AntiDdosInteractor.EVENT_GOOGLE_CAPTCHA)
            context.startActivity(Intent(context, GoogleCaptchaActivity::class.java).apply {
                putExtra("content", throwable.content)
                putExtra("url", throwable.url)
            }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        if (throwable is BlazingFastException) {
            antiDdosInteractor.newExceptionEvent(AntiDdosInteractor.EVENT_BLAZING_FAST)
            context.startActivity(Intent(context, BlazingFastActivity::class.java).apply {
                putExtra("content", throwable.content)
                putExtra("url", throwable.url)
            }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

}