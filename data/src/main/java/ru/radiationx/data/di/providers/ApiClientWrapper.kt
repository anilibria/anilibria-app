package ru.radiationx.data.di.providers

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.data.datasource.remote.address.ApiConfigChanger
import ru.radiationx.data.system.ClientWrapper
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class ApiClientWrapper @Inject constructor(
    private val provider: ApiOkHttpProvider,
    private val configChanger: ApiConfigChanger
) : ClientWrapper(provider) {

    init {
        configChanger
            .observeConfigChanges()
            .onEach {
                set(provider.get())
            }
            .launchIn(GlobalScope)
    }

}