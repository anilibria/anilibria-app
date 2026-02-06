pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "AniLibria"

include(
    ":app-mobile",
    ":data",
    ":shared-ktx",
    ":shared-android-ktx",
    ":searchbar",
    ":app-tv",
    ":shared-app",
    ":quill-di",
    ":media-mobile",
    ":taiwa",
    ":envoy"
)
