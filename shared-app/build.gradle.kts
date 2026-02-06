plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ru.radiationx.shared_app"

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
    implementation(libs.kotlin.stdlib)

    implementation(project(":data"))
    implementation(project(":shared-android-ktx"))
    implementation(project(":quill-di"))
    implementation(libs.androidx.appcompat)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)

    compileOnly(libs.toothpick)
    ksp(libs.toothpick.compiler)

    implementation(libs.cicerone)

    api(libs.yandex.appmetrica)
}
