package ru.radiationx.anilibria

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import io.reactivex.plugins.RxJavaPlugins
import ru.radiationx.anilibria.di.AppModule
import ru.radiationx.data.di.DataModule
import ru.radiationx.shared_app.*
import toothpick.Toothpick
import toothpick.configuration.Configuration

class App : Application() {


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        when (BuildConfig.FLAVOR) {
            "appDev", "store" -> MultiDex.install(this)
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (isMainProcess()) {
            initInMainProcess()
        }
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
