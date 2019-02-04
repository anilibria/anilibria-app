package ru.radiationx.anilibria.ui.navigation

import ru.terrakok.cicerone.Router

open class AppRouter : Router() {
    open fun showSystemMessage(message: String) {
        executeCommands(SystemMessage(message))
    }
}