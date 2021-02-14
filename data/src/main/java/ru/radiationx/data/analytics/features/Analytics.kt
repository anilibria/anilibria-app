package ru.radiationx.data.analytics.features

import ru.radiationx.data.analytics.AnalyticsSender
import toothpick.InjectConstructor

@InjectConstructor
class Analytics(
    private val sender: AnalyticsSender
) {

}