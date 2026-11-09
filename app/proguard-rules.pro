# Later Box Proguard Rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
