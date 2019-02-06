package ru.radiationx.anilibria.navigation

import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.widget.Toast
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command

open class BaseAppNavigator : SupportAppNavigator {

    private var activity: FragmentActivity? = null

    constructor(activity: FragmentActivity?, containerId: Int) : super(activity, containerId) {
        this.activity = activity
    }

    constructor(activity: FragmentActivity?, fragmentManager: FragmentManager?, containerId: Int) : super(activity, fragmentManager, containerId) {
        this.activity = activity
    }


    override fun applyCommand(command: Command?) {
        if (command is SystemMessage) {
            showSystemMessage(command.message)
        } else {
            super.applyCommand(command)
        }
    }

    open fun showSystemMessage(message: String?) {
        Toast.makeText(activity, message.orEmpty(), Toast.LENGTH_SHORT).show()
    }
}