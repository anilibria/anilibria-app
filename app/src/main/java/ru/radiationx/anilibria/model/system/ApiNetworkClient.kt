package ru.radiationx.anilibria.model.system

import ru.radiationx.data.datasource.remote.address.ApiConfig
import javax.inject.Inject

class ApiNetworkClient @Inject constructor(
        private val clientWrapper: ApiClientWrapper,
        private val appCookieJar: AppCookieJar,
        private val apiConfig: ApiConfig
) : Client(clientWrapper, appCookieJar, apiConfig)