package ru.radiationx.anilibria.ui.fragments.configuring

import androidx.lifecycle.ViewModel
import ru.radiationx.data.app.config.ConfiguringInteractor
import javax.inject.Inject

class ConfiguringViewModel @Inject constructor(
    private val configuringInteractor: ConfiguringInteractor
) : ViewModel() {


    init {
    }

    fun continueCheck() {

    }

    fun nextCheck() {
    }

    fun skipCheck() {

    }

    override fun onCleared() {
        super.onCleared()
    }
}