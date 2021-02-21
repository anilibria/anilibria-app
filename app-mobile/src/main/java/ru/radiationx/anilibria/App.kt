package ru.radiationx.anilibria

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import biz.source_code.miniTemplator.MiniTemplator
import com.google.firebase.messaging.FirebaseMessaging
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.plugins.RxJavaPlugins
import ru.radiationx.anilibria.di.AppModule
import ru.radiationx.anilibria.utils.messages.SystemMessenger
import ru.radiationx.data.SchedulersProvider
import ru.radiationx.data.analytics.TimeCounter
import ru.radiationx.data.analytics.features.AppAnalytics
import ru.radiationx.data.datasource.holders.PreferencesHolder
import ru.radiationx.data.di.DataModule
import ru.radiationx.shared.ktx.addTo
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

    private var messengerDisposable = Disposables.disposed()

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

            registerActivityLifecycleCallbacks(object :SimpleActivityLifecycleCallbacks(){
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
        val config = YandexMetricaConfig.newConfigBuilder("48d49aa0-6aad-407e-a738-717a6c77d603").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }

    private fun initInMainProcess() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        RxJavaPlugins.setErrorHandler { throwable ->
            Log.d("S_DEF_LOG", "RxJavaPlugins errorHandler $throwable")
            throwable.printStackTrace()
        }

        initDependencies()

        findTemplate("static_page")?.let { staticPageTemplate = it }
        findTemplate("vk_comments")?.let { vkCommentsTemplate = it }
        findTemplate("video_page")?.let { videoPageTemplate = it }

        val systemMessenger = DI.get(SystemMessenger::class.java)
        val schedulers = DI.get(SchedulersProvider::class.java)

        /*messengerDisposable = systemMessenger
                .observe()
                .observeOn(schedulers.ui())
                .subscribe {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }*/

        val imageDownloader = DI.get(OkHttpImageDownloader::class.java)
        ImageLoaderConfig.init(this, imageDownloader)
        appVersionCheck()

        FirebaseMessaging.getInstance().apply {
            isAutoInitEnabled = true
        }

        val preferencesHolder = DI.get(PreferencesHolder::class.java)
        val disposables = CompositeDisposable()
        preferencesHolder
            .observeNotificationsAll()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ enabled ->
                changeSubscribeStatus(enabled, "all")
            }, {
                it.printStackTrace()
            })
            .addTo(disposables)

        preferencesHolder
            .observeNotificationsService()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ enabled ->
                changeSubscribeStatus(enabled, "service")
                changeSubscribeStatus(enabled, "app_update")
                changeSubscribeStatus(enabled, "config")
            }, {
                it.printStackTrace()
            })
            .addTo(disposables)

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
        try {
            val prefKey = "app.versions.history"
            val defaultPreferences = DI.get(SharedPreferences::class.java)
            val history = defaultPreferences
                .getString(prefKey, "")
                ?.split(";")
                ?.filter { it.isNotBlank() }
                ?.map { it.toInt() }
                ?: emptyList()


            var lastAppCode = 0

            var disorder = false
            history.forEach {
                if (it < lastAppCode) {
                    disorder = true
                }
                lastAppCode = it
            }
            val currentAppCode = ("" + BuildConfig.VERSION_CODE).toInt()

            if (lastAppCode < currentAppCode) {
                if (lastAppCode > 0) {
                    val appMigration = AppMigration(currentAppCode, lastAppCode, history)
                    appMigration.start()
                }

                val list = history.map { it.toString() }.toMutableList()
                list.add(currentAppCode.toString())
                defaultPreferences
                    .edit()
                    .putString(prefKey, TextUtils.join(";", list))
                    .apply()
            }
            if (disorder) {
                val errMsg = "AniLibria: Нарушение порядка версий, программа может работать не стабильно!"
                Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            val errMsg = "Сбой при проверке локальной версии."
            YandexMetrica.reportError(errMsg, ex)
            val uiErr = "$errMsg\nПрограмма может работать не стабильно! Переустановите программу."
            Toast.makeText(this, uiErr, Toast.LENGTH_LONG).show()
        }
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
                MiniTemplator.Builder().build(ByteArrayInputStream("Template error!".toByteArray(charset)), charset)
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
