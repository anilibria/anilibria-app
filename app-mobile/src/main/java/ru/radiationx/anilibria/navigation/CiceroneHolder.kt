package ru.radiationx.anilibria.navigation

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import javax.inject.Inject

class CiceroneHolder @Inject constructor() {
    private val containers: MutableMap<String, Cicerone<Router>> = mutableMapOf()

    fun getCicerone(containerTag: String): Cicerone<Router> {
        if (!containers.containsKey(containerTag)) {
            containers[containerTag] = Cicerone.create(Router())
        }
        return containers.getValue(containerTag)
    }
}