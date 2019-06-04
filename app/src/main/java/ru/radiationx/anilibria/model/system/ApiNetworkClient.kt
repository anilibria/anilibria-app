package ru.radiationx.anilibria.model.system

import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import javax.inject.Inject

class ApiNetworkClient @Inject constructor(
        private val clientWrapper: ApiClientWrapper,
        private val appCookieJar: AppCookieJar,
        private val apiConfig: ApiConfig
) : Client(clientWrapper, appCookieJar, apiConfig)