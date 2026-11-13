# SecureChat ProGuard
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.securemessenger.app.** { *; }
-dontwarn io.github.jan.supabase.**
-dontwarn io.ktor.**
-dontwarn kotlinx.serialization.**
