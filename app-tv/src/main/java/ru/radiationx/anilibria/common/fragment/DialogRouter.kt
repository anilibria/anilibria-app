package ru.radiationx.anilibria.common.fragment

import ru.terrakok.cicerone.BaseRouter
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Forward

class DialogRouter() : Router() {

    fun openDialog(screen: DialogAppScreen) {
        navigateTo(screen)
    }

    fun replaceDialog(screen: DialogAppScreen) {
        replaceScreen(screen)
    }

    fun backDialog() {
        exit()
    }

    fun finishDialogChain() {
        finishChain()
    }
}