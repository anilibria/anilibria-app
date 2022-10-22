package ru.radiationx.data.analytics.profile

import ru.radiationx.data.analytics.features.model.AnalyticsAppTheme

interface AnalyticsThemeProvider {

    fun getTheme(): AnalyticsAppTheme
}