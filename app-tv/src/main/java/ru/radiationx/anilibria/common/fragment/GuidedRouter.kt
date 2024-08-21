package ru.radiationx.anilibria.common.fragment

import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.commands.BackTo

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