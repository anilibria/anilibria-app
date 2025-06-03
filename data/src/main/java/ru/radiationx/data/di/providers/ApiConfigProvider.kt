package ru.radiationx.data.di.providers

import ru.radiationx.data.app.config.ApiConfig
import ru.radiationx.data.app.config.ApiConfigImpl
import javax.inject.Inject
import javax.inject.Provider

// workaround for provide interface of instance
class ApiConfigProvider @Inject constructor(
    private val apiConfigImpl: ApiConfigImpl
) : Provider<ApiConfig> {

    override fun get(): ApiConfig = apiConfigImpl
}