package ru.radiationx.anilibria

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.google.firebase.messaging.FirebaseMessaging
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.mintrocket.lib.mintpermissions.ext.initMintPermissions
import ru.mintrocket.lib.mintpermissions.flows.ext.initMintPermissionsFlow
import ru.radiationx.anilibria.di.AppModule
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.AppAnalytics
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.di.DataModule
import ru.radiationx.data.migration.MigrationDataSource
import ru.radiationx.shared_app.common.SimpleActivityLifecycleCallbacks
import ru.radiationx.shared_app.di.DI
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration

/*  Created by radiationx on 05.11.17. */
class App : Application() {

    private val timeCounter = TimeCounter().apply {
        start()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        when (BuildConfig.FLAVOR) {
            "appDev", "store" -> MultiDex.install(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val timeToCreate = timeCounter.elapsed()
        initYandexAppMetrica()

        if (isMainProcess()) {
            initInMainProcess()

            val timeToInit = timeCounter.elapsed()
            val appAnalytics = DI.get(AppAnalytics::class.java)
            appAnalytics.timeToCreate(timeToCreate)
            appAnalytics.timeToInit(timeToInit)

            registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
                override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                    super.onActivityCreated(p0, p1)
                    val timeToActivity = timeCounter.elapsed()
                    appAnalytics.timeToActivity(timeToActivity)
                    unregisterActivityLifecycleCallbacks(this)
                }
            })
        }
    }

    private fun initYandexAppMetrica() {
        //if (BuildConfig.DEBUG) return
        val config =
            YandexMetricaConfig.newConfigBuilder("48d49aa0-6aad-407e-a738-717a6c77d603").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }

    private fun initInMainProcess() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initDependencies()

        appVersionCheck()

        try {
            FirebaseMessaging.getInstance().apply {
                isAutoInitEnabled = true
            }
        } catch (ex: Throwable) {
            Timber.e(ex)
        }

        val preferencesHolder = DI.get(PreferencesHolder::class.java)

        preferencesHolder
            .observeNotificationsAll()
            .onEach {
                changeSubscribeStatus(it, "all")
            }
            .launchIn(GlobalScope)

        preferencesHolder
            .observeNotificationsService()
            .onEach {
                changeSubscribeStatus(it, "service")
                changeSubscribeStatus(it, "app_update")
                changeSubscribeStatus(it, "config")
            }
            .launchIn(GlobalScope)


        initMintPermissions()
        initMintPermissionsFlow()
    }

    private fun changeSubscribeStatus(enabled: Boolean, topic: String) {
        try {
            FirebaseMessaging.getInstance().apply {
                if (enabled) {
                    subscribeToTopic(topic)
                    subscribeToTopic("android_$topic")
                } else {
                    unsubscribeFromTopic(topic)
                    unsubscribeFromTopic("android_$topic")
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    private fun initDependencies() {
        Toothpick.setConfiguration(Configuration.forProduction())
        val scope = Toothpick.openScope(DI.DEFAULT_SCOPE)
        scope.installModules(AppModule(this), DataModule())
    }

    private fun appVersionCheck() {
        val migrationDataSource = DI.get(MigrationDataSource::class.java)
        migrationDataSource.update()
    }

    private fun isMainProcess() = packageName == getCurrentProcessName()

    private fun getCurrentProcessName(): String? {
        val mypid = android.os.Process.myPid()
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = manager.runningAppProcesses
        return processes.firstOrNull { it.pid == mypid }?.processName
    }

}
