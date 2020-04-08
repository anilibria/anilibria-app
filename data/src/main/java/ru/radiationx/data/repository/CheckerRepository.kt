package ru.radiationx.data.repository

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.api.CheckerApi
import ru.radiationx.data.entity.app.updater.UpdateData
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerRepository @Inject constructor(
    private val schedulers: SchedulersProvider,
    private val checkerApi: CheckerApi
) {

    private val currentDataRelay = BehaviorRelay.create<UpdateData>()

    fun observeUpdate(): Observable<UpdateData> = currentDataRelay.hide()

    fun checkUpdate(versionCode: Int, force: Boolean = false): Single<UpdateData> = Single
        .fromCallable {
            Log.e("CHECKER", "fromCallable0 $versionCode : $force")
            return@fromCallable if (!force && currentDataRelay.hasValue())
                currentDataRelay.value!!
            else
                checkerApi.checkUpdate(versionCode).blockingGet()
        }
        .doOnSuccess {
            /*it.links[0].url = "https://github.com/anilibria/anilibria-app/archive/2.4.4.zip"
            it.links[1].url = "https://github.com/anilibria/anilibria-app/archive/2.4.3.zip"*/

            //it.links[0].url = "https://github.com/anilibria/anilibria-app/archive/2.4.4s.zip"
        }
        .doOnSuccess {
            Log.e("CHECKER", "doOnSuccess $it")
            currentDataRelay.accept(it)
        }
        .subscribeOn(schedulers.io())
        .observeOn(schedulers.ui())

}
