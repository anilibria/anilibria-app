package ru.radiationx.anilibria.ui.fragments.configuring

import androidx.lifecycle.ViewModel
import ru.radiationx.data.app.config.AppConfigUpdater
import javax.inject.Inject

class ConfiguringViewModel @Inject constructor(
    private val configuringInteractor: AppConfigUpdater
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