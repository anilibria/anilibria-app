package ru.radiationx.anilibria.screen.config

import androidx.lifecycle.MutableLiveData
import ru.radiationx.anilibria.common.fragment.GuidedRouter
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.data.interactors.ConfiguringInteractor
import ru.terrakok.cicerone.Router
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
class ConfiguringViewModel(
    private val router: Router,
    private val guidedRouter: GuidedRouter,
    private val apiConfig: ApiConfig,
    private val configuringInteractor: ConfiguringInteractor,
    private val schedulersProvider: SchedulersProvider
) : LifecycleViewModel() {

    private var configuringStarted = false
    val screenStateData = MutableLiveData<ConfigScreenState>()
    val completeEvent = MutableLiveData<Unit>()

    fun startConfiguring() {
        if (configuringStarted) {
            return
        }
        configuringStarted = true
        apiConfig
            .observeNeedConfig()
            .observeOn(schedulersProvider.ui())
            .lifeSubscribe {
                if (!it) {
                    completeEvent.value = Unit
                }
            }

        configuringInteractor
            .observeScreenState()
            .delay(2L, TimeUnit.SECONDS)
            .observeOn(schedulersProvider.ui())
            .lifeSubscribe {
                screenStateData.value = it
            }

        configuringInteractor.initCheck()
    }

    fun endConfiguring() {
        //router.exit()
    }

    fun repeatCheck() = configuringInteractor.repeatCheck()

    fun nextCheck() = configuringInteractor.nextCheck()

    fun skipCheck() = configuringInteractor.skipCheck()

    override fun onCleared() {
        super.onCleared()
        configuringInteractor.finishCheck()
    }
}