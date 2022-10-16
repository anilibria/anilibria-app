package ru.radiationx.anilibria

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import ru.radiationx.anilibria.di.AppModule
import ru.radiationx.data.di.DataModule
import ru.radiationx.shared_app.common.ImageLoaderConfig
import ru.radiationx.shared_app.common.OkHttpImageDownloader
import ru.radiationx.shared_app.di.DI
import toothpick.Toothpick
import toothpick.configuration.Configuration

class App : Application() {

    companion object {

        /*
        * Это нужно, т.к. contentprovider создается до того, как отработает onCreate и будут доступны все зависимости.
        * Такое происходит, когда приложение не запущено, но система уже стучится за данными в contentprovider.
        * Логика такая - подписываемя с блокировкой на эту релейку в методах, которые выполняют запросы (query, insert, etc.)
        * Главное чтобы логика выполнилась после инициализации приложения
        * */
        val appCreateAction = BehaviorRelay.createDefault(false)
    }

    override fun onCreate() {
        super.onCreate()

        initYandexAppMetrica()

        if (isMainProcess()) {
            initInMainProcess()
        }
        appCreateAction.accept(true)
    }

    private fun initYandexAppMetrica() {
        if (BuildConfig.DEBUG) return
        val config = YandexMetricaConfig.newConfigBuilder("48d49aa0-6aad-407e-a738-717a6c77d603").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }

    private fun initInMainProcess() {
        RxJavaPlugins.setErrorHandler { throwable ->
            Log.d("S_DEF_LOG", "RxJavaPlugins errorHandler", throwable)
        }

        initDependencies()

        val imageDownloader = DI.get(OkHttpImageDownloader::class.java)
        ImageLoaderConfig.init(this, imageDownloader)
    }

    private fun initDependencies() {
        Toothpick.setConfiguration(Configuration.forProduction())
        val scope = Toothpick.openScope(DI.DEFAULT_SCOPE)
        scope.installModules(AppModule(this), DataModule(this))
    }


    private fun isMainProcess() = packageName == getCurrentProcessName()

    private fun getCurrentProcessName(): String? {
        val mypid = android.os.Process.myPid()
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = manager.runningAppProcesses
        return processes.firstOrNull { it.pid == mypid }?.processName
    }

}
