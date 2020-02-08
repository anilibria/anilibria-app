package ru.radiationx.anilibria.model.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.entity.app.vital.VitalItem
import ru.radiationx.data.datasource.remote.api.VitalApi
import ru.radiationx.data.SchedulersProvider
import javax.inject.Inject

/**
 * Created by radiationx on 27.01.18.
 */
class VitalRepository @Inject constructor(
        private val schedulers: SchedulersProvider,
        private val vitalApi: VitalApi
) {

    private val currentDataRelay = BehaviorRelay.create<List<VitalItem>>()
    private var currentLoader: Single<List<VitalItem>>? = null

    private fun load() {
        //todo
        /*if (currentLoader == null && !currentDataRelay.hasValue()) {
            currentLoader = vitalApi
                    .loadVital()
                    //.delay(1, TimeUnit.SECONDS)
                    .doOnSuccess {
                        currentDataRelay.accept(it)
                    }

            currentLoader
                    ?.subscribeOn(schedulers.io())
                    ?.observeOn(schedulers.ui())
                    ?.doAfterTerminate {
                        currentLoader = null
                    }
                    ?.subscribe()
        }*/
    }

    fun observeByType(type: VitalItem.VitalType): Observable<List<VitalItem>> {
        return currentDataRelay
                .doOnSubscribe { load() }
                .map { it.filter { it.type == type } }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
    }

    fun observeByRule(rule: VitalItem.Rule) = observeByRules(listOf(rule))

    fun observeByRules(rules: List<VitalItem.Rule>): Observable<List<VitalItem>> {
        return currentDataRelay
                .doOnSubscribe { load() }
                .map { it.filter { it.rules.any { rules.contains(it) } } }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
    }

    fun observeByEvent(rule: VitalItem.EVENT) = observeByEvents(listOf(rule))

    fun observeByEvents(rules: List<VitalItem.EVENT>): Observable<List<VitalItem>> {
        return currentDataRelay
                .doOnSubscribe { load() }
                .map { it.filter { it.events.any { rules.contains(it) } } }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
    }
}