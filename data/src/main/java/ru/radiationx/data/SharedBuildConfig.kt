package ru.radiationx.data

interface SharedBuildConfig {
    val applicationName:String
    val applicationId: String
    val versionName: String
    val versionCode: Int
    val buildDate: String
    val debug: Boolean
    val hasAds: Boolean
}