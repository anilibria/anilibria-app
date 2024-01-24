package ru.radiationx.anilibria

import ru.radiationx.data.SharedBuildConfig
import javax.inject.Inject

class AppBuildConfig @Inject constructor() : SharedBuildConfig {

    override val applicationId: String = BuildConfig.APPLICATION_ID

    override val versionName: String = BuildConfig.VERSION_NAME

    override val versionCode: Int = BuildConfig.VERSION_CODE

    override val buildDate: String = BuildConfig.BUILD_DATE

    override val debug: Boolean = BuildConfig.DEBUG

    override val hasAds: Boolean = BuildConfig.HAS_ADS
}