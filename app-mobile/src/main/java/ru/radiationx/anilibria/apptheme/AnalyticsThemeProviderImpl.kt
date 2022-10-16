package ru.radiationx.anilibria.apptheme

import ru.radiationx.data.analytics.features.model.AnalyticsAppTheme
import ru.radiationx.data.analytics.profile.AnalyticsThemeProvider
import toothpick.InjectConstructor

@InjectConstructor
class AnalyticsThemeProviderImpl(
    private val appThemeController: AppThemeController
) : AnalyticsThemeProvider {

    override fun getTheme(): AnalyticsAppTheme {
        return appThemeController.getMode().toAnalytics()
    }

    private fun AppThemeMode.toAnalytics() = when (this) {
        AppThemeMode.LIGHT -> AnalyticsAppTheme.LIGHT
        AppThemeMode.DARK -> AnalyticsAppTheme.DARK
        AppThemeMode.SYSTEM -> AnalyticsAppTheme.SYSTEM
    }
}