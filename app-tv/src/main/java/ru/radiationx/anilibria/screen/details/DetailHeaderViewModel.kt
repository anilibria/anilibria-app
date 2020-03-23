package ru.radiationx.anilibria.screen.details

import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.DetailDataConverter
import ru.radiationx.anilibria.common.LibriaDetails
import ru.radiationx.anilibria.common.LibriaDetailsRow
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.interactors.ReleaseInteractor
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class DetailHeaderViewModel(
    private val releaseInteractor: ReleaseInteractor,
    private val converter: DetailDataConverter
) : LifecycleViewModel() {

    var releaseId: Int = -1

    val releaseData = MutableLiveData<LibriaDetails>()

    override fun onCreate() {
        super.onCreate()

        (releaseInteractor.getFull(releaseId) ?: releaseInteractor.getItem(releaseId))?.also {
            releaseData.value = converter.toDetail(it)
        }

        releaseInteractor
            .observeFull(releaseId)
            //.delay(2000, TimeUnit.MILLISECONDS)
            .map { converter.toDetail(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe {
                Log.e("kekeke", "observeFull")
                releaseData.value = it
            }

    }
}