package ru.radiationx.anilibria.model.system

import ru.radiationx.anilibria.di.providers.MainOkHttpProvider
import javax.inject.Inject

class MainClientWrapper @Inject constructor(
        provider: MainOkHttpProvider
) : ClientWrapper(provider)