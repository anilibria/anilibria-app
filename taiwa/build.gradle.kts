plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "taiwa"

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
    implementation(project(":envoy"))
    implementation(libs.kotlin.stdlib)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)

    implementation(libs.google.material)
    implementation(libs.google.flexbox)

    implementation(libs.viewbindingpropertydelegate)
}
