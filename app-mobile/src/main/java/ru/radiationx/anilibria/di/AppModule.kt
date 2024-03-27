package ru.radiationx.anilibria.di

import android.app.Application
import android.content.Context
import ru.mintrocket.lib.mintpermissions.MintPermissions
import ru.mintrocket.lib.mintpermissions.flows.MintPermissionsFlow
import ru.radiationx.anilibria.AppBuildConfig
import ru.radiationx.anilibria.AppMigrationExecutor
import ru.radiationx.anilibria.BuildConfig
import ru.radiationx.anilibria.MobileCheckerSources
import ru.radiationx.anilibria.apptheme.AnalyticsThemeProviderImpl
import ru.radiationx.anilibria.apptheme.AppThemeController
import ru.radiationx.anilibria.apptheme.AppThemeControllerImpl
import ru.radiationx.anilibria.ads.NativeAdsRepository
import ru.radiationx.anilibria.navigation.CiceroneHolder
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.activities.player.PlayerDataSourceProvider
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.ui.common.LinkRouter
import ru.radiationx.anilibria.ui.common.Templates
import ru.radiationx.anilibria.ui.fragments.comments.VkCommentsCss
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.radiationx.anilibria.utils.ShortcutHelper
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.data.analytics.AnalyticsErrorReporter
import ru.radiationx.data.analytics.AnalyticsSender
import ru.radiationx.data.analytics.profile.AnalyticsProfile
import ru.radiationx.data.analytics.profile.AnalyticsThemeProvider
import ru.radiationx.data.datasource.remote.common.CheckerReserveSources
import ru.radiationx.data.migration.MigrationExecutor
import ru.radiationx.quill.QuillModule
import ru.radiationx.shared_app.analytics.CodecsProfileAnalytics
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

class AppModule(application: Application) : QuillModule() {


    init {
        instance {
            application
        }
        instance<Context> {
            application
        }

        singleImpl<SharedBuildConfig, AppBuildConfig>()
        singleImpl<CheckerReserveSources, MobileCheckerSources>()
        singleImpl<MigrationExecutor, AppMigrationExecutor>()

        single<SystemMessenger>()

        single<ShortcutHelper>()

        instance {
            MintPermissions.controller
        }
        instance {
            MintPermissionsFlow.dialogFlow
        }


        single<Templates>()
        single<VkCommentsCss>()
        singleImpl<AppThemeController, AppThemeControllerImpl>()
        singleImpl<AnalyticsThemeProvider, AnalyticsThemeProviderImpl>()

        val ciceroneHolder by lazy { CiceroneHolder() }
        val cicerone by lazy { ciceroneHolder.getCicerone("root") }
        instance {
            ciceroneHolder
        }
        instance {
            cicerone.router
        }
        instance {
            cicerone.navigatorHolder
        }


        single<DimensionsProvider>()

        singleImpl<ILinkHandler, LinkRouter>()
        singleImpl<IErrorHandler, ErrorHandler>()
        singleImpl<LibriaImageLoader, CoilLibriaImageLoaderImpl>()

        /* Ads */
        single<NativeAdsRepository>()

        /* Analytics */
        single<CodecsProfileAnalytics>()

        single<AppMetricaAnalyticsSender>()
        single<AppMetricaAnalyticsProfile>()
        single<AppMetricaErrorReporter>()

        single<LoggingAnalyticsSender>()
        single<LoggingAnalyticsProfile>()
        single<LoggingErrorReporter>()

        if (BuildConfig.DEBUG) {
            singleImpl<AnalyticsSender, CombinedAnalyticsSender>()
            singleImpl<AnalyticsProfile, CombinedAnalyticsProfile>()
            singleImpl<AnalyticsErrorReporter, CombinedErrorReporter>()
        } else {
            singleImpl<AnalyticsSender, AppMetricaAnalyticsSender>()
            singleImpl<AnalyticsProfile, AppMetricaAnalyticsProfile>()
            singleImpl<AnalyticsErrorReporter, AppMetricaErrorReporter>()
        }

        /* Player */
        single<PlayerDataSourceProvider>()
    }

}