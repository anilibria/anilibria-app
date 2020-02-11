package ru.radiationx.anilibria

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import androidx.multidex.MultiDex
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import io.reactivex.plugins.RxJavaPlugins
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
        val scope = Toothpick.openScope(Scopes.APP)
        scope.installModules(AppModule(this), DataModule(this))

        Log.e("lalala", "initDependencies ${Toothpick.openScope(Scopes.APP)}")
    }


    private fun isMainProcess() = packageName == getCurrentProcessName()

    private fun getCurrentProcessName(): String? {
        val mypid = android.os.Process.myPid()
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = manager.runningAppProcesses
        return processes.firstOrNull { it.pid == mypid }?.processName
    }

}
