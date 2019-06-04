package ru.radiationx.anilibria.model.system

import okhttp3.OkHttpClient
import ru.radiationx.anilibria.di.providers.ApiOkHttpProvider
import ru.radiationx.anilibria.di.providers.MainOkHttpProvider
import ru.radiationx.anilibria.model.data.remote.address.ApiConfigChanger
import javax.inject.Inject
import javax.inject.Provider

class MainClientWrapper @Inject constructor(
        provider: MainOkHttpProvider
) : ClientWrapper(provider)