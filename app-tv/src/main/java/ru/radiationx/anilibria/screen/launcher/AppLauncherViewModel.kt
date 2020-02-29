package ru.radiationx.anilibria.screen.launcher

import ru.radiationx.anilibria.ConfigScreen
import ru.radiationx.anilibria.FlowScreen
import ru.radiationx.anilibria.screen.LifecycleViewModel
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.datasource.remote.address.ApiConfig
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.Screen
import toothpick.InjectConstructor

@InjectConstructor
class AppLauncherViewModel(
    private val apiConfig: ApiConfig,
    private val schedulersProvider: SchedulersProvider,
    private val router: Router
) : LifecycleViewModel() {

    override fun onCreate() {
        super.onCreate()
        if (apiConfig.needConfig) {
            router.newRootScreen(ConfigScreen())
        } else {
            router.newRootScreen(FlowScreen())
        }
    }
}