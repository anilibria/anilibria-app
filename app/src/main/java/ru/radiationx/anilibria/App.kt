package ru.radiationx.anilibria

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import biz.source_code.miniTemplator.MiniTemplator
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.yandex.metrica.YandexMetrica
import io.reactivex.plugins.RxJavaPlugins
import ru.radiationx.anilibria.model.data.holders.CookieHolder
import ru.radiationx.anilibria.model.data.holders.PreferencesHolder
import ru.radiationx.anilibria.model.data.holders.UserHolder
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.api.*
import ru.radiationx.anilibria.model.data.storage.*
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.*
import ru.radiationx.anilibria.model.system.ApiUtils
import ru.radiationx.anilibria.model.system.AppSchedulers
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.presentation.ErrorHandler
import ru.radiationx.anilibria.presentation.ErrorHandlerImpl
import ru.radiationx.anilibria.presentation.LinkHandler
import ru.radiationx.anilibria.presentation.LinkRouter
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

/*  Created by radiationx on 05.11.17. */
class App : Application() {
    companion object {

        init {
            //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        lateinit var instance: App
            private set

        lateinit var navigation: Navigation
        lateinit var injections: Injections
    }

    lateinit var articleTemplate: MiniTemplator
    lateinit var staticPageTemplate: MiniTemplator

    override fun onCreate() {
        super.onCreate()
        instance = this

        //Fabric.with(this, Crashlytics())

        YandexMetrica.activate(applicationContext, "48d49aa0-6aad-407e-a738-717a6c77d603")
        YandexMetrica.enableActivityAutoTracking(this)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        RxJavaPlugins.setErrorHandler { throwable ->
            Log.d("S_DEF_LOG", "RxJavaPlugins errorHandler " + throwable)
            throwable.printStackTrace()
        }
        navigation = Navigation()
        injections = Injections(this, navigation.root.router)
        findTemplate("article")?.let { articleTemplate = it }
        findTemplate("static_page")?.let { staticPageTemplate = it }
        initImageLoader(this)
        appVersionCheck()
    }

    private fun appVersionCheck() {
        try {
            val prefKey = "app.versions.history"
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val history = TextUtils.split(sharedPreferences.getString(prefKey, ""), ";").map { it.toInt() }

            var lastAppCode = 0

            var disorder = false
            history.forEach {
                if (it < lastAppCode) {
                    disorder = true
                }
                lastAppCode = it
            }
            val currentAppCode = ("" + BuildConfig.VERSION_CODE).toInt()
            val appMigration = AppMigration(currentAppCode, lastAppCode, history)

            try {
                appMigration.start()
            } catch (ex: Throwable) {
                ex.printStackTrace()
                val errMsg = "Сбой при миграции данных программы."
                YandexMetrica.reportError(errMsg, ex)
                Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show()
                throw Exception(ex)
            }

            if (lastAppCode < currentAppCode) {
                val list = history.map { it.toString() }.toMutableList()
                list.add(currentAppCode.toString())
                sharedPreferences.edit().putString(prefKey, TextUtils.join(";", list)).apply()
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

    class Navigation {
        val root = NavigationRoot()
        val local = LocalCiceroneHolder()
    }

    /* Костыле-колесо чтобы не тащить toothpick или dagger2 */
    class Injections(context: Context, router: Router) {
        val dimensionsProvider = DimensionsProvider()
        val schedulers: SchedulersProvider = AppSchedulers()
        private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val appPreferences: PreferencesHolder = PreferencesStorage(sharedPreferences)
        val episodesCheckerStorage = EpisodesCheckerStorage(sharedPreferences)
        val historyStorage = HistoryStorage(sharedPreferences)

        val linkHandler: LinkHandler = LinkRouter()
        val errorHandler: ErrorHandler = ErrorHandlerImpl(context, router)

        private val cookieHolder: CookieHolder = CookiesStorage(sharedPreferences)
        private val userHolder: UserHolder = UserStorage(sharedPreferences)

        val client: IClient = Client(cookieHolder, userHolder)
        val apiUtils: IApiUtils = ApiUtils()

        private val authApi = AuthApi(client, apiUtils)
        private val articleApi = ArticleApi(client, apiUtils)
        private val releaseApi = ReleaseApi(client, apiUtils)
        private val searchApi = SearchApi(client, apiUtils)
        private val pageApi = PageApi(client, apiUtils)
        private val vitalApi = VitalApi(client, apiUtils)
        private val checkerApi = CheckerApi(client, apiUtils)

        val authRepository = AuthRepository(schedulers, authApi, userHolder, cookieHolder)
        val articleRepository = ArticleRepository(schedulers, articleApi)
        val releaseRepository = ReleaseRepository(schedulers, releaseApi)
        val searchRepository = SearchRepository(schedulers, searchApi)
        val pageRepository = PageRepository(schedulers, pageApi)
        val vitalRepository = VitalRepository(schedulers, vitalApi)
        val checkerRepository = CheckerRepository(schedulers, checkerApi)
        val historyRepository = HistoryRepository(schedulers, historyStorage)

        val releaseInteractor = ReleaseInteractor(releaseRepository, episodesCheckerStorage, appPreferences, schedulers)
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
                containers[containerTag] = Cicerone.create()
            }
            return containers.getValue(containerTag)
        }
    }

}
