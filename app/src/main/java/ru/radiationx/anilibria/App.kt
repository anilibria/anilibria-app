package ru.radiationx.anilibria

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.support.multidex.MultiDex
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
import com.yandex.metrica.YandexMetricaConfig
import io.reactivex.plugins.RxJavaPlugins
import ru.radiationx.anilibria.model.data.holders.*
import ru.radiationx.anilibria.model.data.remote.IAntiDdosErrorHandler
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.api.*
import ru.radiationx.anilibria.model.data.remote.parsers.*
import ru.radiationx.anilibria.model.data.storage.*
import ru.radiationx.anilibria.model.interactors.AntiDdosInteractor
import ru.radiationx.anilibria.model.interactors.ReleaseInteractor
import ru.radiationx.anilibria.model.repository.*
import ru.radiationx.anilibria.model.system.ApiUtils
import ru.radiationx.anilibria.model.system.AppSchedulers
import ru.radiationx.anilibria.model.system.SchedulersProvider
import ru.radiationx.anilibria.model.system.messages.SystemMessenger
import ru.radiationx.anilibria.presentation.common.IErrorHandler
import ru.radiationx.anilibria.presentation.common.ILinkHandler
import ru.radiationx.anilibria.ui.common.AntiDdosErrorHandler
import ru.radiationx.anilibria.ui.common.ErrorHandler
import ru.radiationx.anilibria.ui.common.LinkRouter
import ru.radiationx.anilibria.utils.DimensionsProvider
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
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

        lateinit var navigation: Navigation
        lateinit var injections: Injections
    }

    lateinit var articleTemplate: MiniTemplator
    lateinit var staticPageTemplate: MiniTemplator
    lateinit var vkCommentsTemplate: MiniTemplator

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
        if (BuildConfig.FLAVOR.equals("appDev")) {
            MultiDex.install(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        //Fabric.with(this, Crashlytics())
        val config = YandexMetricaConfig.newConfigBuilder("48d49aa0-6aad-407e-a738-717a6c77d603").build()
        YandexMetrica.activate(applicationContext, config)
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
        findTemplate("vk_comments")?.let { vkCommentsTemplate = it }

        val disposable = injections
                .systemMessenger
                .observe()
                .observeOn(injections.schedulers.ui())
                .subscribe {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }

        initImageLoader(this)
        appVersionCheck()
    }

    private fun appVersionCheck() {
        try {
            val prefKey = "app.versions.history"
            val history = injections
                    .defaultPreferences
                    .getString(prefKey, "")
                    .split(";")
                    .filter { it.isNotBlank() }
                    .map { it.toInt() }

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
                injections
                        .defaultPreferences
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

    class Navigation {
        val root = NavigationRoot()
        val local = LocalCiceroneHolder()
    }

    /* Костыле-колесо чтобы не тащить toothpick или dagger2 */
    class Injections(context: Context, router: Router) {

        val dimensionsProvider = DimensionsProvider()
        val schedulers: SchedulersProvider = AppSchedulers()
        val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val dataStoragePreferences = context.getSharedPreferences("${context.packageName}_datastorage", Context.MODE_PRIVATE)

        private val preferencesStorage = PreferencesStorage(defaultPreferences)

        val appPreferences: PreferencesHolder = preferencesStorage
        val episodesCheckerStorage: EpisodesCheckerHolder = EpisodesCheckerStorage(dataStoragePreferences)
        val historyStorage: HistoryHolder = HistoryStorage(dataStoragePreferences)
        val releaseUpdateStorage: ReleaseUpdateHolder = ReleaseUpdateStorage(dataStoragePreferences, schedulers)
        val genresHolder: GenresHolder = GenresStorage(dataStoragePreferences)
        val yearsHolder: YearsHolder = YearsStorage(dataStoragePreferences)

        val antiDdosInteractor = AntiDdosInteractor(schedulers)
        val systemMessenger = SystemMessenger()

        val linkHandler: ILinkHandler = LinkRouter()
        val errorHandler: IErrorHandler = ErrorHandler(systemMessenger)
        val antiDdosErrorHandler: IAntiDdosErrorHandler = AntiDdosErrorHandler(antiDdosInteractor, context)

        val cookieHolder: CookieHolder = CookiesStorage(defaultPreferences)
        val userHolder: UserHolder = UserStorage(defaultPreferences)
        val authHolder: AuthHolder = AuthStorage()

        val appThemeHolder: AppThemeHolder = preferencesStorage

        val client: IClient = Client(cookieHolder, userHolder, context, antiDdosErrorHandler)
        val apiUtils: IApiUtils = ApiUtils()

        private val articleParser = ArticleParser(apiUtils)
        private val authParser = AuthParser(apiUtils)
        private val checkerParser = CheckerParser(apiUtils)
        private val commentParser = CommentParser(apiUtils)
        private val favoriteParser = FavoriteParser(apiUtils)
        private val pagesParser = PagesParser(apiUtils)
        private val profileParser = ProfileParser(apiUtils)
        private val releaseParser = ReleaseParser(apiUtils)
        private val searchParser = SearchParser(apiUtils)
        private val vitalParser = VitalParser(apiUtils)
        private val youtubeParser = YoutubeParser(apiUtils)

        private val articleApi = ArticleApi(client, articleParser)
        private val authApi = AuthApi(client, authParser)
        private val checkerApi = CheckerApi(client, checkerParser)
        private val commentApi = CommentApi(client, commentParser)
        private val favoriteApi = FavoriteApi(client, releaseParser)
        private val releaseApi = ReleaseApi(client, releaseParser)
        private val searchApi = SearchApi(client, releaseParser, searchParser)
        private val pageApi = PageApi(client, pagesParser)
        private val vitalApi = VitalApi(client, vitalParser)
        private val youtubeApi = YoutubeApi(client, youtubeParser)

        val authRepository = AuthRepository(schedulers, authApi, userHolder, cookieHolder)
        val articleRepository = ArticleRepository(schedulers, articleApi, commentApi)
        val releaseRepository = ReleaseRepository(schedulers, releaseApi, releaseUpdateStorage)
        val searchRepository = SearchRepository(schedulers, searchApi, genresHolder, yearsHolder, releaseUpdateStorage)
        val pageRepository = PageRepository(schedulers, pageApi)
        val vitalRepository = VitalRepository(schedulers, vitalApi)
        val checkerRepository = CheckerRepository(schedulers, checkerApi)
        val historyRepository = HistoryRepository(schedulers, historyStorage)
        val favoriteRepository = FavoriteRepository(schedulers, favoriteApi)
        val youtubeRepository = YoutubeRepository(schedulers, youtubeApi)
        val commentsRepository = CommentsRepository(schedulers, commentApi)

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
        private val cicerone: Cicerone<Router> = Cicerone.create(Router())

        val router: Router = cicerone.router
        val holder: NavigatorHolder = cicerone.navigatorHolder
    }

    class LocalCiceroneHolder {
        private val containers: MutableMap<String, Cicerone<Router>> = mutableMapOf()

        fun getCicerone(containerTag: String): Cicerone<Router> {
            if (!containers.containsKey(containerTag)) {
                containers[containerTag] = Cicerone.create(Router())
            }
            return containers.getValue(containerTag)
        }
    }

}
