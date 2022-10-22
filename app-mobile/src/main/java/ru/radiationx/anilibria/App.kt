package ru.radiationx.anilibria

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import biz.source_code.miniTemplator.MiniTemplator
import com.google.firebase.messaging.FirebaseMessaging
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.anilibria.di.AppModule
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.AppAnalytics
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.di.DataModule
import ru.radiationx.data.migration.MigrationDataSource
import ru.radiationx.shared_app.common.ImageLoaderConfig
import ru.radiationx.shared_app.common.OkHttpImageDownloader
import ru.radiationx.shared_app.common.SimpleActivityLifecycleCallbacks
import ru.radiationx.shared_app.di.DI
import toothpick.Toothpick
import toothpick.configuration.Configuration
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.Charset

/*  Created by radiationx on 05.11.17. */
class App : Application() {

    companion object {

        init {
            //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        lateinit var instance: App
            private set

    }

    private val timeCounter = TimeCounter().apply {
        start()
    }

    lateinit var staticPageTemplate: MiniTemplator
    lateinit var vkCommentsTemplate: MiniTemplator
    lateinit var videoPageTemplate: MiniTemplator

    val vkCommentCssFixLight: String by lazy {
        assets.open("styles/vk_comments_fix_light.css").bufferedReader().use {
            it.readText()
        }
    }

    val vkCommentCssFixDark: String by lazy {
        assets.open("styles/vk_comments_fix_dark.css").bufferedReader().use {
            it.readText()
        }
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
        instance = this
        initYandexAppMetrica()

        if (isMainProcess()) {
            initInMainProcess()
            val timeToInit = timeCounter.elapsed()
            val appAnalytics = DI.get(AppAnalytics::class.java)
            appAnalytics.timeToCreate(timeToCreate)
            appAnalytics.timeToInit(timeToInit)

            registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
                override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
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

        initDependencies()

        findTemplate("static_page")?.let { staticPageTemplate = it }
        findTemplate("vk_comments")?.let { vkCommentsTemplate = it }
        findTemplate("video_page")?.let { videoPageTemplate = it }

        val imageDownloader = DI.get(OkHttpImageDownloader::class.java)
        ImageLoaderConfig.init(this, imageDownloader)
        appVersionCheck()

        FirebaseMessaging.getInstance().apply {
            isAutoInitEnabled = true
        }

        val preferencesHolder = DI.get(PreferencesHolder::class.java)
        //todo tr-274 check working
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

    }

    private fun changeSubscribeStatus(enabled: Boolean, topic: String) {
        FirebaseMessaging.getInstance().apply {
            if (enabled) {
                subscribeToTopic(topic)
                subscribeToTopic("android_$topic")
            } else {
                unsubscribeFromTopic(topic)
                unsubscribeFromTopic("android_$topic")
            }
        }
    }

    private fun initDependencies() {
        Toothpick.setConfiguration(Configuration.forProduction())
        val scope = Toothpick.openScope(DI.DEFAULT_SCOPE)
        scope.installModules(AppModule(this), DataModule(this))
    }

    private fun appVersionCheck() {
        val migrationDataSource = DI.get(MigrationDataSource::class.java)
        migrationDataSource.update()
    }

    private fun findTemplate(name: String): MiniTemplator? {
        var template: MiniTemplator? = null
        try {
            val stream = assets.open("templates/$name.html")
            val charset: Charset = Charset.forName("utf-8")
            template = try {
                MiniTemplator.Builder().build(stream, charset)
            } catch (e: Exception) {
                e.printStackTrace()
                MiniTemplator.Builder()
                    .build(ByteArrayInputStream("Template error!".toByteArray(charset)), charset)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return template
    }

    private fun isMainProcess() = packageName == getCurrentProcessName()

    private fun getCurrentProcessName(): String? {
        val mypid = android.os.Process.myPid()
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = manager.runningAppProcesses
        return processes.firstOrNull { it.pid == mypid }?.processName
    }

}
