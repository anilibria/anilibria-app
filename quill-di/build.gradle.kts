plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "ru.radiationx.quill"

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

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.recyclerview)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.toothpick.runtime)
    ksp(libs.toothpick.compiler)

    api(libs.javax.inject)
}
