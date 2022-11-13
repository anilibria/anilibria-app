package ru.radiationx.anilibria

import ru.radiationx.data.migration.MigrationExecutor
import toothpick.InjectConstructor

@InjectConstructor
class AppMigrationExecutor : MigrationExecutor {

    override fun execute(current: Int, lastSaved: Int, history: List<Int>) {
    }
}