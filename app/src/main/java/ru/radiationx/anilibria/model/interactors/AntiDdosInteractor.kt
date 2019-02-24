package ru.radiationx.anilibria.model.interactors

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject

class AntiDdosInteractor @Inject constructor(
        private val schedulers: SchedulersProvider
) {
    companion object {
        const val EVENT_GOOGLE_CAPTCHA = "google_captcha"
        const val EVENT_BLAZING_FAST = "blazingfast"
    }

    var isHardChecked = false

    private val exceptionRelay = PublishRelay.create<String>()
    private val completeRelay = PublishRelay.create<String>()

    fun observerExceptionEvents(): Observable<String> = exceptionRelay
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun observerCompleteEvents(): Observable<String> = completeRelay
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun newExceptionEvent(event: String) {
        exceptionRelay.accept(event)
    }

    fun newCompleteEvent(event: String) {
        completeRelay.accept(event)
    }
}