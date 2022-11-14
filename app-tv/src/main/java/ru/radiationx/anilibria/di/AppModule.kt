package ru.radiationx.anilibria.di

import android.content.Context
import ru.mintrocket.lib.mintpermissions.MintPermissions
import ru.mintrocket.lib.mintpermissions.MintPermissionsController
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsDialogFlow
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsFlow
import ru.radiationx.anilibria.AppBuildConfig
import ru.radiationx.anilibria.AppMigrationExecutor
import ru.radiationx.anilibria.TvCheckerSources
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.AnalyticsErrorReporter
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import ru.radiationx.data.migration.MigrationExecutor
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
import toothpick.config.Module

class AppModule(context: Context) : Module() {


    init {
        bind(Context::class.java).toInstance(context)
        bind(SharedBuildConfig::class.java).to(AppBuildConfig::class.java).singleton()
        bind(CheckerReserveSources::class.java).to(TvCheckerSources::class.java).singleton()
        bind(MigrationExecutor::class.java).to(AppMigrationExecutor::class.java).singleton()

        bind(LibriaImageLoader::class.java).to(CoilLibriaImageLoaderImpl::class.java).singleton()

        bind(MintPermissionsController::class.java).toInstance(MintPermissions.controller)
        bind(MintPermissionsDialogFlow::class.java).toInstance(MintPermissionsFlow.dialogFlow)

        bind(AppMetricaAnalyticsSender::class.java).singleton()
        bind(AppMetricaAnalyticsProfile::class.java).singleton()
        bind(AppMetricaErrorReporter::class.java).singleton()

        bind(LoggingAnalyticsSender::class.java).singleton()
        bind(LoggingAnalyticsProfile::class.java).singleton()
        bind(LoggingErrorReporter::class.java).singleton()

        bind(AnalyticsSender::class.java).to(CombinedAnalyticsSender::class.java).singleton()
        bind(AnalyticsProfile::class.java).to(CombinedAnalyticsProfile::class.java).singleton()
        bind(AnalyticsErrorReporter::class.java).to(CombinedErrorReporter::class.java).singleton()
    }

}