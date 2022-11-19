package ru.radiationx.anilibria.presentation.configuring

import androidx.lifecycle.ViewModel
import moxy.InjectViewState
import ru.radiationx.data.interactors.ConfiguringInteractor
import javax.inject.Inject

@InjectViewState
class ConfiguringViewModel @Inject constructor(
    private val configuringInteractor: ConfiguringInteractor
) : ViewModel() {

    val state = configuringInteractor.observeScreenState()

    init {
        configuringInteractor.initCheck()
    }

    fun continueCheck() = configuringInteractor.repeatCheck()

    fun nextCheck() = configuringInteractor.nextCheck()

    fun skipCheck() = configuringInteractor.skipCheck()

    override fun onCleared() {
        super.onCleared()
        configuringInteractor.finishCheck()
    }
}