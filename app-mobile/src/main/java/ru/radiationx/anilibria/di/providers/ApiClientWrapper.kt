package ru.radiationx.anilibria.di.providers

import android.util.Log
import ru.radiationx.data.datasource.remote.address.ApiConfigChanger
import ru.radiationx.data.system.ClientWrapper
import javax.inject.Inject

class ApiClientWrapper @Inject constructor(
        private val provider: ApiOkHttpProvider,
        private val configChanger: ApiConfigChanger
) : ClientWrapper(provider) {

    init {
        Log.d("bobobo", "init ApiClientWrapper")
        val disposable = configChanger
                .observeConfigChanges()
                .doOnSubscribe {

                    Log.d("boboob", "ApiClientWrapper doOnSubscribe")
                }
                .subscribe {
                    Log.d("boboob", "ApiClientWrapper subscribe")
                    set(provider.get())
                }
    }

}