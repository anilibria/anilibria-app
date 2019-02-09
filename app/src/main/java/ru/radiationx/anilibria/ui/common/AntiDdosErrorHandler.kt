package ru.radiationx.anilibria.ui.common

import android.content.Context
import android.content.Intent
import ru.radiationx.anilibria.model.data.BlazingFastException
import ru.radiationx.anilibria.model.data.GoogleCaptchaException
import ru.radiationx.anilibria.model.data.remote.IAntiDdosErrorHandler
import ru.radiationx.anilibria.model.interactors.AntiDdosInteractor
import ru.radiationx.anilibria.ui.activities.BlazingFastActivity
import ru.radiationx.anilibria.ui.activities.GoogleCaptchaActivity
import javax.inject.Inject

class AntiDdosErrorHandler @Inject constructor(
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