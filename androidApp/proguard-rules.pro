# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Koin DI
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Room and SQLite
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.** { *; }
-dontwarn androidx.room.**

# Game entities and models
-keep class com.squishout.game.data.entity.** { *; }
-keep class com.squishout.engine.model.** { *; }

# Compose Multiplatform Resources
-keep class org.jetbrains.compose.resources.** { *; }