-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-allowaccessmodification
-dontpreverify

-keepattributes SourceFile,LineNumberTable

-dontnote **
-dontwarn ru.radiationx.**

# okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**
-dontnote okio.**

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**


-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.app.** { *; }
-keep interface android.support.v7.app.** { *; }

-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

# Note that if we could use kapt to generate registries, possible to get rid of this
-keepattributes *Annotation*
# Do not obfuscate classes with Injected Constructors
-keepnames class * { @javax.inject.Inject <init>(...); }
-keepclasseswithmembernames class * { @javax.inject.Inject <init>(...); }
# Do not obfuscate classes with Injected Fields
-keepclasseswithmembernames class * { @javax.inject.Inject <fields>; }
# Do not obfuscate classes with Injected Methods
-keepclasseswithmembernames class * { @javax.inject.Inject <methods>; }
-keep @android.support.annotation.Keep class *
-dontwarn javax.inject.**
-dontwarn javax.annotation.**
-keep class **__Factory { *; }
-keep class **__MemberInjector { *; }

-adaptclassstrings
-keep class toothpick.** { *; }

-keep @javax.inject.Singleton class *


# idk why it is applied from library...
# added for viewbindingpropertydelegate 1.5.9
-keep,allowoptimization class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(...);
}