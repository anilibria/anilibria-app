package ru.radiationx.anilibria

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Html
import android.util.Log
import biz.source_code.miniTemplator.MiniTemplator
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import io.reactivex.plugins.RxJavaPlugins
import org.json.JSONObject
import ru.radiationx.anilibria.model.data.holders.AuthHolder
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.api.ArticleApi
import ru.radiationx.anilibria.model.data.remote.api.AuthApi
import ru.radiationx.anilibria.model.data.remote.api.ReleaseApi
import ru.radiationx.anilibria.model.data.remote.api.SearchApi
import ru.radiationx.anilibria.model.data.storage.AuthStorage
import ru.radiationx.anilibria.model.data.storage.CookiesStorage
import ru.radiationx.anilibria.model.repository.ArticleRepository
import ru.radiationx.anilibria.model.repository.AuthRepository
import ru.radiationx.anilibria.model.repository.ReleaseRepository
import ru.radiationx.anilibria.model.repository.SearchRepository
import ru.radiationx.anilibria.model.system.ApiUtils
import ru.radiationx.anilibria.model.system.AppSchedulers
import ru.radiationx.anilibria.model.system.SchedulersProvider
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
        injections = Injections(this)
        navigation = Navigation()
        findTemplate("article")?.let { articleTemplate = it }
        initImageLoader(this)
        val vkurl = "https://oauth.logo_vk.com/authorize?client_id=5315207&amp;redirect_uri=https%3A%2F%2Fwww.anilibria.tv%2F%3Fauth_service_id%3DVKontakte&amp;scope=friends,notify,offline,email&amp;response_type=code&amp;state=site_id%3Ds1%26backurl%3D%252F%253Fcheck_key%253D5ef024816f25632438e9ccfbeffd0dd4%2526logout_butt%253D%2525D0%252592%2525D1%25258B%2525D0%2525B9%2525D1%252582%2525D0%2525B8%26redirect_url%3D%252F"

        try {
            Log.e("SUKA", "Try vk0: " + vkurl)
            Log.e("SUKA", "Try vk1: " + JSONObject.quote(vkurl))
            Log.e("SUKA", "Try vk2: " + JSONObject.wrap(vkurl))
            Log.e("SUKA", "Try vk3: " + JSONObject("{\"url\":\"$vkurl\"}").getString("url"))
            Log.e("SUKA", "Try vk4: " + Html.fromHtml(vkurl))
            Log.e("SUKA", "Try vk5: " + vkurl.replace("&amp;".toRegex(), "&"))

        } catch (e: Exception) {
            e.printStackTrace()
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

    class Navigation {
        val root = NavigationRoot()
        val local = LocalCiceroneHolder()
    }

    /* Костыле-колесо чтобы не тащить toothpick или dagger2 */
    class Injections(context: Context) {
        val schedulers: SchedulersProvider = AppSchedulers()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val cookieHolder: CookieHolder = CookiesStorage(sharedPreferences)
        val authHolder: AuthHolder = AuthStorage(sharedPreferences)

        val client: IClient = Client(cookieHolder)
        val apiUtils: IApiUtils = ApiUtils()

        var authApi = AuthApi(client, apiUtils)
        var articleApi = ArticleApi(client, apiUtils)
        var releaseApi = ReleaseApi(client, apiUtils)
        var searchApi = SearchApi(client, apiUtils)

        val authRepository = AuthRepository(schedulers, authApi, authHolder, cookieHolder)
        val articleRepository = ArticleRepository(schedulers, articleApi)
        val releaseRepository = ReleaseRepository(schedulers, releaseApi)
        val searchRepository = SearchRepository(schedulers, searchApi)
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
