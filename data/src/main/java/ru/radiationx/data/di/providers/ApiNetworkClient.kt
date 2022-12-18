package ru.radiationx.data.di.providers

import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.system.Client
import javax.inject.Inject

class ApiNetworkClient @Inject constructor(
    private val clientWrapper: ApiClientWrapper,
    private val sharedBuildConfig: SharedBuildConfig
) : Client(clientWrapper, sharedBuildConfig)