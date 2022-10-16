package ru.radiationx.data.di.providers

import android.util.Log
import ru.radiationx.data.datasource.remote.address.ApiConfigChanger
import ru.radiationx.data.system.ClientWrapper
import javax.inject.Inject

class ApiClientWrapper @Inject constructor(
        private val provider: ApiOkHttpProvider,
        private val configChanger: ApiConfigChanger
) : ClientWrapper(provider) {

    init {
        val disposable = configChanger
                .observeConfigChanges()
                .subscribe {
                    set(provider.get())
                }
    }

}