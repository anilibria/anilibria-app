package ru.radiationx.anilibria.common.fragment

import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.commands.BackTo

class GuidedRouter() : Router() {

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