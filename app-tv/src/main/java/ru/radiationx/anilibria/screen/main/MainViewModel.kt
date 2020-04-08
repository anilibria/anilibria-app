package ru.radiationx.anilibria.screen.main

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.radiationx.anilibria.common.BaseRowsViewModel
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.entity.common.AuthState
import ru.radiationx.data.repository.AuthRepository
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class MainViewModel(
    private val authRepository: AuthRepository
) : BaseRowsViewModel() {

    companion object {
        const val FEED_ROW_ID = 1L
        const val SCHEDULE_ROW_ID = 2L
        const val FAVORITE_ROW_ID = 3L
        const val YOUTUBE_ROW_ID = 4L

    }

    override val rowIds: List<Long> = listOf(FEED_ROW_ID, FAVORITE_ROW_ID, SCHEDULE_ROW_ID, YOUTUBE_ROW_ID)

    override val availableRows: MutableList<Long> = mutableListOf(FEED_ROW_ID, SCHEDULE_ROW_ID, YOUTUBE_ROW_ID)

    override fun onCreate() {
        super.onCreate()

        authRepository
            .observeUser()
            .observeOn(AndroidSchedulers.mainThread())
            .lifeSubscribe {
                updateAvailableRow(FAVORITE_ROW_ID, it.authState == AuthState.AUTH)
            }
    }
}