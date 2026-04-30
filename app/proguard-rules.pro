-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep line numbers for crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin
-keep class kotlin.Metadata { *; }

# AdMob / Google Mobile Ads SDK
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.internal.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# UMP (consent) SDK
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# Firebase Crashlytics
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase Performance
-keep class com.google.firebase.perf.** { *; }
-dontwarn com.google.firebase.perf.**

# Glance / AppWidgets - keep receivers referenced from manifest
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class com.charles.flashlight.widget.** { *; }

# Compose tooling sometimes needs reflection on @Preview composables in debug;
# release stripping is fine, no extra rules required.
