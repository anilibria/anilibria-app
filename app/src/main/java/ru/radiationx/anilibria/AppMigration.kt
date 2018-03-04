package ru.radiationx.anilibria

/**
 * Created by radiationx on 26.02.18.
 */
class AppMigration(
        private val current: Int,
        private val last: Int,
        private val history: List<Int>
) {

    fun start() {
        if (current == 20) {
            App.injections.appPreferences.setReleaseRemind(true)
        }
    }
}