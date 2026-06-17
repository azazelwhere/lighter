# Keep model classes used by Room
-keep class com.lighter.browser.data.** { *; }
-keep class com.lighter.browser.spoofing.** { *; }

# Kotlin metadata
-keep class kotlin.Metadata { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
