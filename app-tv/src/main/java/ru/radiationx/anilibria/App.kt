package ru.radiationx.anilibria

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import io.reactivex.plugins.RxJavaPlugins
import ru.radiationx.anilibria.di.AppModule
import ru.radiationx.data.di.DataModule
import ru.radiationx.shared_app.common.ImageLoaderConfig
import ru.radiationx.shared_app.common.OkHttpImageDownloader
import ru.radiationx.shared_app.di.DI
import toothpick.Toothpick
import toothpick.configuration.Configuration

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initYandexAppMetrica()

        if (isMainProcess()) {
            initInMainProcess()
        }
    }

    private fun initYandexAppMetrica() {
        if (BuildConfig.DEBUG) return
        val config = YandexMetricaConfig.newConfigBuilder("48d49aa0-6aad-407e-a738-717a6c77d603").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }

    private fun initInMainProcess() {
        RxJavaPlugins.setErrorHandler { throwable ->
            Log.d("S_DEF_LOG", "RxJavaPlugins errorHandler $throwable")
            throwable.printStackTrace()
        }

        initDependencies()

        val imageDownloader = DI.get(OkHttpImageDownloader::class.java)
        ImageLoaderConfig.init(this, imageDownloader)
    }

    private fun initDependencies() {
        Toothpick.setConfiguration(Configuration.forProduction())
        val scope = Toothpick.openScope(DI.DEFAULT_SCOPE)
        scope.installModules(AppModule(this), DataModule(this))

        Log.e("lalala", "initDependencies ${Toothpick.openScope(DI.DEFAULT_SCOPE)}")
    }


    private fun isMainProcess() = packageName == getCurrentProcessName()

    private fun getCurrentProcessName(): String? {
        val mypid = android.os.Process.myPid()
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = manager.runningAppProcesses
        return processes.firstOrNull { it.pid == mypid }?.processName
    }

}
