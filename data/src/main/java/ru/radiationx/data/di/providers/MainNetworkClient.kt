package ru.radiationx.data.di.providers

import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.system.Client
import javax.inject.Inject

class MainNetworkClient @Inject constructor(
    clientWrapper: MainClientWrapper,
    sharedBuildConfig: SharedBuildConfig,
) : Client(clientWrapper, sharedBuildConfig)