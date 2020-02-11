package ru.radiationx.anilibria

import android.content.Context
import ru.radiationx.data.SharedBuildConfig
import ru.radiationx.shared_app.OkHttpImageDownloader
import toothpick.config.Module

class AppModule(context: Context) : Module() {


    init {
        bind(Context::class.java).toInstance(context)
        bind(SharedBuildConfig::class.java).to(AppBuildConfig::class.java).singletonInScope()
        bind(OkHttpImageDownloader::class.java).singletonInScope()
    }

}