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

    private fun AppThemeController.AppThemeMode.toAnalytics() = when (this) {
        AppThemeController.AppThemeMode.LIGHT -> AnalyticsAppTheme.LIGHT
        AppThemeController.AppThemeMode.DARK -> AnalyticsAppTheme.DARK
        AppThemeController.AppThemeMode.SYSTEM -> AnalyticsAppTheme.SYSTEM
    }
}