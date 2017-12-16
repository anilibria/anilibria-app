package ru.radiationx.anilibria

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer

import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

/*  Created by radiationx on 05.11.17. */
class App : Application() {
    companion object {
        lateinit var instance: App
            private set

        lateinit var navigation: Navigation
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        navigation = Navigation()
        initImageLoader(this)
    }

    class Navigation {
        val root = NavigationRoot()
        val local = LocalCiceroneHolder()
    }

    class NavigationRoot {
        private val cicerone: Cicerone<Router> = Cicerone.create()

        val router: Router = cicerone.router
        val holder: NavigatorHolder = cicerone.navigatorHolder
    }

    class LocalCiceroneHolder {
        private val containers: MutableMap<String, Cicerone<Router>> = mutableMapOf()

        fun getCicerone(containerTag: String): Cicerone<Router> {
            if (!containers.containsKey(containerTag)) {
                containers.put(containerTag, Cicerone.create())
            }
            return containers.getValue(containerTag)
        }
    }


    private val defaultOptionsUIL: DisplayImageOptions.Builder = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .handler(Handler())
            .displayer(FadeInBitmapDisplayer(500, true, true, false))

    private fun initImageLoader(context: Context) {
        val config = ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(UsingFreqLimitedMemoryCache(5 * 1024 * 1024)) // 5 Mb
                .diskCacheFileNameGenerator(HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(defaultOptionsUIL.build())
                .build()
        ImageLoader.getInstance().init(config)
    }
}
