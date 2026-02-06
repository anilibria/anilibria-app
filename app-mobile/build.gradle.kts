import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.appmetrica)
}

fun getDateTime(): String {
    val df = SimpleDateFormat("dd MMMMM yyyy")
    return "${df.format(Date())} г."
}

android {
    namespace = "ru.radiationx.anilibria"

    compileSdk = libs.versions.app.compile.sdk.version.get().toInt()

    defaultConfig {
        applicationId = "ru.radiationx.anilibria.app"
        minSdk = libs.versions.app.min.sdk.version.get().toInt()
        targetSdk = libs.versions.app.target.sdk.version.get().toInt()
        versionCode = libs.versions.mobile.version.code.get().toInt()
        versionName = libs.versions.mobile.version.name.get()
        buildConfigField("String", "BUILD_DATE", "\"${getDateTime()}\"")
        vectorDrawables.useSupportLibrary = true
    }
    androidResources {
        localeFilters += listOf("ru")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    val localProperties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }
    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("storeFile"))
            storePassword = localProperties.getProperty("storePassword")
            keyAlias = localProperties.getProperty("keyAlias")
            keyPassword = localProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += listOf("type")
    productFlavors {
        create("app") {
            dimension = "type"
            buildConfigField("boolean", "HAS_ADS", "true")
            buildConfigField("boolean", "FOR_RUSTORE", "false")
        }

        create("rustore") {
            dimension = "type"
            buildConfigField("boolean", "HAS_ADS", "true")
            buildConfigField("boolean", "FOR_RUSTORE", "true")
            versionName = "${libs.versions.mobile.version.name.get()}-rustore"
        }

        create("store") {
            dimension = "type"
            applicationId = "tv.anilibria.store"
            buildConfigField("boolean", "HAS_ADS", "false")
            buildConfigField("boolean", "FOR_RUSTORE", "false")
            versionName = "${libs.versions.mobile.version.name.get()}-lite"
        }
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val inputApkName = buildString {
            append("${project.name}-")
            variant.productFlavors.forEach { (dimension, flavor) ->
                append("${flavor}-")
            }
            variant.buildType?.also { append(it) }
            append(".apk")
        }
        val inputApkPath = buildString {
            append("outputs/apk/")
            variant.flavorName?.also { append("$it/") }
            variant.buildType?.also { append("$it/") }
            append(inputApkName)
        }
        val inputPath = project.layout.buildDirectory.file(inputApkPath).get().asFile

        val appName = "AniLiberty"
        val versionName = variant.outputs[0].versionName.get()
        val outputApkName = "${appName}_v${versionName}.apk"

        val buildName = variant.name.capitalize()
        tasks.register<Copy>("copy${buildName}Apk") {
            description = "Copies $buildName APK to another folder"
            group = "custom"
            from(inputPath) {
                rename { outputApkName }
            }
            into("${rootProject.rootDir}/release-apks/")
            dependsOn(tasks.named("assemble$buildName"))
        }
    }
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.version.get().toInt())
}

appmetrica {
    val localProperties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }
    val propApiKey = localProperties.getProperty("appmetrica_post_api_key", "")
    setPostApiKey(propApiKey)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.constraintlayout)

    implementation(project(":data"))
    implementation(project(":shared-android-ktx"))
    implementation(project(":shared-app"))
    implementation(project(":quill-di"))
    implementation(project(":media-mobile"))
    implementation(project(":taiwa"))
    implementation(project(":envoy"))

    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.preference)

    implementation(libs.kotlin.coroutines.core)

    implementation(libs.cicerone)

    implementation(project(":searchbar"))

    implementation(libs.google.flexbox)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.session)

    implementation(libs.minitemplator)

    implementation(libs.adapterdelegates)

    implementation(libs.mintpermissions)
    implementation(libs.mintpermissions.flows)

    implementation(libs.materialprogressbar)

    compileOnly(libs.toothpick)
    ksp(libs.toothpick.compiler)

    implementation(libs.androidnetworktools)


    implementation(libs.firebase.messaging)
    implementation(libs.firebase.core)

    implementation(libs.viewbindingpropertydelegate)

    implementation(libs.yandex.mobileads)
}

apply(plugin = libs.plugins.gms.get().pluginId)