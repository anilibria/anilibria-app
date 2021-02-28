package ru.radiationx.anilibria

import com.nostra13.universalimageloader.core.ImageLoader
import ru.radiationx.shared_app.di.DI
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.migration.MigrationExecutor
import toothpick.InjectConstructor
import javax.inject.Inject

@InjectConstructor
class AppMigrationExecutor(
    private val appPreferences: PreferencesHolder
) : MigrationExecutor {


    override fun execute(current: Int, lastSaved: Int, history: List<Int>) {
        if (current == 20) {
            appPreferences.setReleaseRemind(true)
        }
        if (lastSaved <= 52) {
            ImageLoader.getInstance().clearDiskCache()
        }
    }
}