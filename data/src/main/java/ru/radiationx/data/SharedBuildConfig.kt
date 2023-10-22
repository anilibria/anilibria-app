package ru.radiationx.data

interface SharedBuildConfig {
    val applicationId: String
    val versionName: String
    val versionCode: Int
    val buildDate: String
    val debug: Boolean
}