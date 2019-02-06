package ru.radiationx.anilibria.navigation

import ru.terrakok.cicerone.Router

open class AppRouter : Router() {
    open fun showSystemMessage(message: String) {
        executeCommands(SystemMessage(message))
    }
}