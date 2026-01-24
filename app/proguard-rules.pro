# --- 1. KOTLIN & COMPOSE ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- 2. ENUMS ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- 3. DATA LAYER WHITELIST (NUCLEAR OPTION) ---
# Prevent R8 from touching ANY data class, repository, or entity
-keep class com.example.sakartveloguide.data.** { *; }
-keep class com.example.sakartveloguide.domain.model.** { *; }
-keep class com.example.sakartveloguide.data.repository.** { *; }

# Preserve GSON annotations
-keepattributes Signature, RuntimeVisibleAnnotations, AnnotationDefault
-keep class com.google.gson.annotations.SerializedName { *; }
-keep class com.google.gson.** { *; }

# --- 4. ROOM & HILT ---
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.sakartveloguide.SakartveloApp { *; }
-keep class dagger.hilt.** { *; }

# --- 5. THIRD PARTY LIBS ---
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class org.maplibre.** { *; }
-keep class coil.** { *; }
-dontwarn okio.**
-dontwarn org.maplibre.**