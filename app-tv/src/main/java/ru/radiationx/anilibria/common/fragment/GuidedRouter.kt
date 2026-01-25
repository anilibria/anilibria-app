package ru.radiationx.anilibria.common.fragment

import com.github.terrakok.cicerone.BackTo
import com.github.terrakok.cicerone.Router

class GuidedRouter : Router() {

    fun open(screen: GuidedAppScreen) {
        navigateTo(screen)
    }

    fun replace(screen: GuidedAppScreen) {
        replaceScreen(screen)
    }

    fun close() {
        exit()
    }

    fun finishGuidedChain() {
        //finishChain()
        executeCommands(BackTo(null))
    }
}