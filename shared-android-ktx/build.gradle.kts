plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.radiationx.shared.ktx.android"

    compileSdk = libs.versions.app.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.tv.min.sdk.version.get().toInt()
    }
    lint {
        targetSdk = libs.versions.app.target.sdk.version.get().toInt()
    }
    testOptions {
        targetSdk = libs.versions.app.target.sdk.version.get().toInt()
    }
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.version.get().toInt())
}

dependencies {
    api(project(":shared-ktx"))
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlin.stdlib)

    api(libs.androidx.core)
    api(libs.androidx.collection.ktx)
    api(libs.androidx.fragment)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.livedata.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.palette)
}

