package ru.radiationx.anilibria.model.repository

import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.entity.app.other.LinkMenuItem
import ru.radiationx.anilibria.model.datasource.holders.MenuHolder
import ru.radiationx.anilibria.model.datasource.remote.api.MenuApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import javax.inject.Inject

class MenuRepository @Inject constructor(
        private val menuHolder: MenuHolder,
        private val menuApi: MenuApi,
        private val schedulers: SchedulersProvider
) {

    fun observeMenu(): Observable<List<LinkMenuItem>> = menuHolder.observe()

    fun getMenu(): Single<List<LinkMenuItem>> = menuApi
            .getMenu()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .doOnSuccess { menuHolder.save(it) }
}