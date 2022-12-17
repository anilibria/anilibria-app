package ru.radiationx.anilibria.screen.config

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.radiationx.data.entity.common.ConfigScreenState
import ru.radiationx.data.interactors.ConfiguringInteractor
import toothpick.InjectConstructor

@InjectConstructor
class ConfiguringViewModel(
    private val apiConfig: ApiConfig,
    private val configuringInteractor: ConfiguringInteractor
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
            .onEach {
                if (!it) {
                    completeEvent.value = Unit
                }
            }
            .launchIn(viewModelScope)

        configuringInteractor
            .observeScreenState()
            .onEach {
                delay(2000L)
            }
            .onEach {
                screenStateData.value = it
            }
            .launchIn(viewModelScope)

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