package ru.radiationx.anilibria.di

import android.content.Context
import ru.mintrocket.lib.mintpermissions.MintPermissions
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsFlow
import ru.radiationx.anilibria.AppBuildConfig
import ru.radiationx.anilibria.TvCheckerSources
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.AnalyticsErrorReporter
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.app.updater.CheckerReserveSources
import ru.radiationx.quill.QuillModule
import ru.radiationx.shared_app.analytics.errors.AppMetricaErrorReporter
import ru.radiationx.shared_app.analytics.errors.CombinedErrorReporter
import ru.radiationx.shared_app.analytics.errors.LoggingErrorReporter
import ru.radiationx.shared_app.analytics.events.AppMetricaAnalyticsSender
import ru.radiationx.shared_app.analytics.events.CombinedAnalyticsSender
import ru.radiationx.shared_app.analytics.events.LoggingAnalyticsSender
import ru.radiationx.shared_app.analytics.profile.AppMetricaAnalyticsProfile
import ru.radiationx.shared_app.analytics.profile.CombinedAnalyticsProfile
import ru.radiationx.shared_app.analytics.profile.LoggingAnalyticsProfile
import ru.radiationx.shared_app.imageloader.LibriaImageLoader
import ru.radiationx.shared_app.imageloader.impls.CoilLibriaImageLoaderImpl

class AppModule(context: Context) : QuillModule() {


    init {
        instance { context }

        singleImpl<SharedBuildConfig, AppBuildConfig>()
        singleImpl<CheckerReserveSources, TvCheckerSources>()

        singleImpl<LibriaImageLoader, CoilLibriaImageLoaderImpl>()

        instance {
            MintPermissions.controller
        }

        instance {
            MintPermissionsFlow.dialogFlow
        }

        single<AppMetricaAnalyticsSender>()
        single<AppMetricaAnalyticsProfile>()
        single<AppMetricaErrorReporter>()

        single<LoggingAnalyticsSender>()
        single<LoggingAnalyticsProfile>()
        single<LoggingErrorReporter>()

        singleImpl<AnalyticsSender, CombinedAnalyticsSender>()
        singleImpl<AnalyticsProfile, CombinedAnalyticsProfile>()
        singleImpl<AnalyticsErrorReporter, CombinedErrorReporter>()
    }

}