package ru.radiationx.anilibria

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import kotlinx.coroutines.flow.MutableStateFlow
import ru.radiationx.anilibria.di.AppModule
import ru.radiationx.data.di.DataModule
import ru.radiationx.quill.Quill
import timber.log.Timber
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
        val appCreateAction = MutableStateFlow(false)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        initYandexAppMetrica()

        if (isMainProcess()) {
            initInMainProcess()
        }
        appCreateAction.value = true
    }

    private fun initYandexAppMetrica() {
        val config =
            YandexMetricaConfig.newConfigBuilder("48d49aa0-6aad-407e-a738-717a6c77d603").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }

    private fun initInMainProcess() {
        initDependencies()
    }

    private fun initDependencies() {
        Toothpick.setConfiguration(Configuration.forProduction())
        Quill.getRootScope().installModules(AppModule(this), DataModule())
    }


    private fun isMainProcess() = packageName == getCurrentProcessName()

    private fun getCurrentProcessName(): String? {
        val mypid = android.os.Process.myPid()
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = manager.runningAppProcesses
        return processes?.firstOrNull { it.pid == mypid }?.processName
    }

}
