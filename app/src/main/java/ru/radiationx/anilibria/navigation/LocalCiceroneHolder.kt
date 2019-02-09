package ru.radiationx.anilibria.navigation

import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class LocalCiceroneHolder @Inject constructor() {
    private val containers: MutableMap<String, Cicerone<Router>> = mutableMapOf()

    fun getCicerone(containerTag: String): Cicerone<Router> {
        if (!containers.containsKey(containerTag)) {
            containers[containerTag] = Cicerone.create(Router())
        }
        return containers.getValue(containerTag)
    }
}