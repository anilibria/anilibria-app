package ru.radiationx.data.di.providers

import ru.radiationx.data.system.ClientWrapper
import javax.inject.Inject

class SimpleClientWrapper @Inject constructor(
        provider: SimpleOkHttpProvider,
) : ClientWrapper(provider)