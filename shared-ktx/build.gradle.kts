plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.radiationx.shared.ktx"

    compileSdk = libs.versions.app.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.tv.min.sdk.version.get().toInt()
    }
    lint {
        targetSdk = 35
    }
    testOptions {
        targetSdk = 35
    }
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.version.get().toInt())
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
}

