package ru.radiationx.anilibria.model.system

import ru.radiationx.anilibria.model.datasource.remote.address.ApiConfig
import javax.inject.Inject

class MainNetworkClient @Inject constructor(
        private val clientWrapper: MainClientWrapper,
        private val appCookieJar: AppCookieJar,
        private val apiConfig: ApiConfig
) : Client(clientWrapper, appCookieJar, apiConfig)