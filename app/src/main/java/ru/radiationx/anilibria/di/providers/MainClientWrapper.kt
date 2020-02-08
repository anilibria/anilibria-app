package ru.radiationx.anilibria.di.providers

import ru.radiationx.data.system.ClientWrapper
import javax.inject.Inject

class MainClientWrapper @Inject constructor(
        provider: MainOkHttpProvider
) : ClientWrapper(provider)