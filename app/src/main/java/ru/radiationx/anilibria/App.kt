package ru.radiationx.anilibria

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import biz.source_code.miniTemplator.MiniTemplator
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import io.reactivex.plugins.RxJavaPlugins
import ru.radiationx.anilibria.data.api.modules.ArticleApi
import ru.radiationx.anilibria.data.api.modules.ReleaseApi
import ru.radiationx.anilibria.data.api.modules.SearchApi
import ru.radiationx.anilibria.data.client.Client
import ru.radiationx.anilibria.data.client.IClient
import ru.radiationx.anilibria.data.repository.ArticleRepository
import ru.radiationx.anilibria.data.repository.ReleaseRepository
import ru.radiationx.anilibria.data.repository.SearchRepository
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.Charset

/*  Created by radiationx on 05.11.17. */
class App : Application() {
    companion object {
        lateinit var instance: App
            private set

        lateinit var navigation: Navigation
        lateinit var injections: Injections
    }

    lateinit var articleTemplate: MiniTemplator

    override fun onCreate() {
        super.onCreate()
        instance = this
        RxJavaPlugins.setErrorHandler { throwable ->
            Log.d("SUKA", "RxJavaPlugins errorHandler " + throwable)
            throwable.printStackTrace()
        }
        injections = Injections()
        navigation = Navigation()
        findTemplate("article")?.let { articleTemplate = it }
        initImageLoader(this)
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

    class Navigation {
        val root = NavigationRoot()
        val local = LocalCiceroneHolder()
    }

    /* Костыле-колесо чтобы не тащить toothpick или dagger2 */
    class Injections {
        private val client: IClient = Client()
        var articleApi = ArticleApi(client)
        var releaseApi = ReleaseApi(client)
        var searchApi = SearchApi(client)

        val articleRepository = ArticleRepository(articleApi)
        val releaseRepository = ReleaseRepository(releaseApi)
        val searchRepository = SearchRepository(searchApi)
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

    /* Cicerone навигация
    * root - для активити
    * local - для табов, типа как в семпле cicerone
    * */
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

}