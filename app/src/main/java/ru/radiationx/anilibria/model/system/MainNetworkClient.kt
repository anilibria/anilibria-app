package ru.radiationx.anilibria.model.system

import ru.radiationx.anilibria.di.qualifier.MainClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import javax.inject.Inject

class MainNetworkClient @Inject constructor(
        private val clientWrapper: MainClientWrapper,
        private val appCookieJar: AppCookieJar,
        private val apiConfig: ApiConfig
) : Client(clientWrapper, appCookieJar, apiConfig)