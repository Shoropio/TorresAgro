# Firebase, Play Services and AndroidX libraries ship consumer ProGuard rules.
# Keep app models used reflectively by Firestore/serialization/Room.
-keep class com.torresagro.app.data.firebase.** { *; }
-keep class com.torresagro.app.data.local.entity.** { *; }
-keep class com.torresagro.app.domain.model.** { *; }

# Keep Kotlin serialization metadata for domain payloads.
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-keep class kotlinx.serialization.** { *; }

# OSMDroid uses reflection for some providers and configuration paths.
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**
