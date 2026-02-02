plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.radiationx.media.mobile"

    compileSdk = libs.versions.app.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.tv.min.sdk.version.get().toInt()
    }

    buildFeatures {
        viewBinding = true
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
    implementation(project(":shared-android-ktx"))

    implementation(libs.timber)

    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)

    implementation(libs.media3.exoplayer)

    implementation(libs.viewbindingpropertydelegate)
}
