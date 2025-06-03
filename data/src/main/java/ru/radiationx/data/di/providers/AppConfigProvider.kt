package ru.radiationx.data.di.providers

import ru.radiationx.data.app.config.AppConfig
import ru.radiationx.data.app.config.AppConfigImpl
import javax.inject.Inject
import javax.inject.Provider

// workaround for provide interface of instance
class AppConfigProvider @Inject constructor(
    private val appConfigImpl: AppConfigImpl
) : Provider<AppConfig> {

    override fun get(): AppConfig = appConfigImpl
}