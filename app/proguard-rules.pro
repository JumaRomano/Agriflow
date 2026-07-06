# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Keep all project application classes, activities, services, viewmodels, and models
-keep class com.agriflow.app.** { *; }

# Kotlinx Serialization Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class kotlinx.serialization.json.** { *; }
-keep class * implements kotlinx.serialization.KSerializer { *; }
-keepclassmembers class * {
    *** Companion;
    *** $serializer;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Retrofit Rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.** <methods>;
}
-dontwarn retrofit2.**

# OkHttp Rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers class okhttp3.internal.publicsuffix.PublicSuffixDatabase {
    *** color;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt & Dagger Rules
-keep class dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class *::class { *; }
-keep class *__HiltBinder* { *; }
-keep class *__HiltKeyModule* { *; }
-keep class *__HiltActivityModule* { *; }
-keep class *__HiltViewModelModule* { *; }
-keep class *__HiltSingletonModule* { *; }
-keep class *__HiltFragmentModule* { *; }
-keep class *__HiltServiceModule* { *; }
-keep class *__HiltViewModule* { *; }
-keep class *__HiltBroadcastReceiverModule* { *; }
-keep class *__HiltLocalBroadcastReceiverModule* { *; }
-keep class *__HiltModule* { *; }
-keep class *__HiltWrapper* { *; }
-dontwarn dagger.hilt.internal.GeneratedComponentManager