# Add project specific ProGuard rules here.
-keepattributes *Annotation*

# ── BuildConfig ──
# NOTE: Không keep BuildConfig để R8 obfuscate API key fields.
# Chỉ keep các field cần thiết (không bao gồm OPENROUTER_API_KEY)
-keep class com.lichso.app.BuildConfig {
    public static final boolean DEBUG;
    public static final java.lang.String APPLICATION_ID;
    public static final int VERSION_CODE;
    public static final java.lang.String VERSION_NAME;
    public static final java.lang.String BUILD_TYPE;
}

# ── Hilt / Dagger ──
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.internal.codegen.**

# ── ViewModel ──
-keep class * extends androidx.lifecycle.ViewModel { *; }

# ── Room ──
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ── Kotlin Coroutines ──
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ── Gson (JSON parsing) ──
-keepattributes Signature
-keepattributes EnclosingMethod
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ── OpenRouter AI API models ──
-keep class com.lichso.app.data.remote.ChatMessage { *; }
-keep class com.lichso.app.data.remote.OpenRouterRequest { *; }
-keep class com.lichso.app.data.remote.OpenRouterResponse { *; }
-keep class com.lichso.app.data.remote.OpenRouterResponse$Choice { *; }
-keep class com.lichso.app.data.remote.OpenRouterResponse$MessageContent { *; }

# ── Weather API models (Open-Meteo) ──
-keep class com.lichso.app.data.remote.OpenMeteoResponse { *; }
-keep class com.lichso.app.data.remote.OpenMeteoCurrent { *; }
-keep class com.lichso.app.domain.model.WeatherInfo { *; }
-keep class com.lichso.app.domain.model.LocationInfo { *; }

# ── OkHttp / Okio ──
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ── WorkManager ──
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

# ── Firebase / Google Sign-In ──
# Only keep what's actually used (Firebase Auth, Credentials)
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── Kotlin ──
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# ── DataStore ──
-dontwarn androidx.datastore.**
