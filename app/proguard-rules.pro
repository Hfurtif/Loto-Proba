# Règles R8 / ProGuard pour LotoProba (com.lotoproba.app)

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations

# Kotlin metadata & coroutines
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Room (Entités, DAOs, Base de données générée)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class com.lotoproba.app.data.db.** { *; }
-keepclassmembers class com.lotoproba.app.data.model.** { *; }
-dontwarn androidx.room.**

# OkHttp & Okio
-keepattributes AnnotationDefault
-keepclassmembers class * {
    @okhttp3.* <fields>;
    @okhttp3.* <methods>;
}
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# BuildConfig & org.json
-keep class com.lotoproba.app.BuildConfig { *; }
-keep class org.json.** { *; }
