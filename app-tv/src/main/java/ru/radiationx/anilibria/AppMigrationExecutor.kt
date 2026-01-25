package ru.radiationx.anilibria

import ru.radiationx.data.migration.MigrationExecutor
import javax.inject.Inject

class AppMigrationExecutor @Inject constructor() : MigrationExecutor {

    override fun execute(current: Int, lastSaved: Int, history: List<Int>) {
    }
}