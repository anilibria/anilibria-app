package ru.radiationx.anilibria.model.system

import okhttp3.OkHttpClient
import ru.radiationx.anilibria.di.qualifier.ApiClient
import ru.radiationx.anilibria.di.qualifier.MainClient
import ru.radiationx.anilibria.model.data.remote.address.ApiConfig
import javax.inject.Inject

class ApiNetworkClient @Inject constructor(
        @ApiClient private val clientWrapper: ClientWrapper,
        private val appCookieJar: AppCookieJar,
        private val apiConfig: ApiConfig
) : Client(clientWrapper, appCookieJar, apiConfig)