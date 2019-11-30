package ru.radiationx.anilibria.model.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.entity.app.other.LinkMenuItem
import ru.radiationx.anilibria.model.data.holders.MenuHolder
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import ru.radiationx.anilibria.model.data.remote.api.MenuApi
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.ui.adapters.MenuListItem
import java.util.concurrent.TimeUnit
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