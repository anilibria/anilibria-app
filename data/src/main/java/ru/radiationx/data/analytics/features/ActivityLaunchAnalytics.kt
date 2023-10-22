package ru.radiationx.data.analytics.features

import android.app.Activity
import android.os.Bundle
import ru.radiationx.data.analytics.AnalyticsConstants
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.features.extensions.toParam
import toothpick.InjectConstructor

@InjectConstructor
class ActivityLaunchAnalytics(
    private val sender: AnalyticsSender,
) {

    fun launchFromHistory(activity: Activity, savedState: Bundle?) {
        val intent = activity.intent
        sender.send(
            AnalyticsConstants.activity_from_history,
            activity::class.simpleName.toParam("name"),
            (intent != null).toParam("has_intent"),
            (intent?.extras != null).toParam("has_extra"),
            intent?.extras?.keySet()?.toList().toParam("extra_keys"),
            (savedState != null).toParam("has_saved"),
            savedState?.keySet()?.toList().toParam("saved_keys")
        )
    }

}